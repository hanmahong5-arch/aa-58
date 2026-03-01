package com.aionemu.tools.navconverter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.RecastVectors;

/**
 * Converts legacy Aion .nav binary files to Detour .bin format.
 *
 * Legacy .nav format (little-endian):
 *   int vertexCount
 *   float[vertexCount * 3] vertices (x, y, z per vertex)
 *   int triangleCount
 *   For each triangle:
 *     int[3] vertexIndices
 *     int[3] edgeConnections (-1 = no neighbor)
 *
 * Output: standard Detour NavMesh binary (.bin)
 *
 * Usage: java -jar nav-converter.jar <input-dir> <output-dir>
 *   input-dir:  directory containing {worldId}.nav files
 *   output-dir: directory to write {worldId}.bin files
 */
public class NavConverter {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: java -jar nav-converter.jar <input-dir> <output-dir>");
			System.out.println("  input-dir:  directory with .nav files (e.g., ./data/nav/)");
			System.out.println("  output-dir: directory for .bin output (e.g., ./data/nav/recast/)");
			System.exit(1);
		}

		Path inputDir = Paths.get(args[0]);
		Path outputDir = Paths.get(args[1]);

		if (!Files.isDirectory(inputDir)) {
			System.err.println("Input directory does not exist: " + inputDir.toAbsolutePath());
			System.exit(1);
		}
		Files.createDirectories(outputDir);

		int success = 0;
		int failed = 0;

		try (Stream<Path> navFiles = Files.list(inputDir).filter(p -> p.toString().endsWith(".nav"))) {
			for (Path navFile : (Iterable<Path>) navFiles::iterator) {
				String fileName = navFile.getFileName().toString();
				String worldId = fileName.substring(0, fileName.length() - 4);
				Path outFile = outputDir.resolve(worldId + ".bin");
				try {
					convertNavFile(navFile, outFile);
					success++;
					System.out.println("[OK]   " + fileName + " -> " + outFile.getFileName());
				} catch (Exception e) {
					failed++;
					System.err.println("[FAIL] " + fileName + ": " + e.getMessage());
				}
			}
		}

		System.out.println("\nConversion complete. Success: " + success + ", Failed: " + failed);
	}

	private static void convertNavFile(Path navFile, Path outFile) throws IOException {
		byte[] raw = Files.readAllBytes(navFile);
		ByteBuffer buf = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);

		// read vertices
		int vertexCount = buf.getInt();
		if (vertexCount <= 0 || vertexCount > 1_000_000) {
			throw new IOException("Invalid vertex count: " + vertexCount);
		}

		float[] verts = new float[vertexCount * 3];
		for (int i = 0; i < verts.length; i++) {
			verts[i] = buf.getFloat();
		}

		// read triangles
		int triangleCount = buf.getInt();
		if (triangleCount <= 0 || triangleCount > 1_000_000) {
			throw new IOException("Invalid triangle count: " + triangleCount);
		}

		int[] tris = new int[triangleCount * 3];
		int[][] adjacency = new int[triangleCount][3];
		for (int t = 0; t < triangleCount; t++) {
			tris[t * 3] = buf.getInt();
			tris[t * 3 + 1] = buf.getInt();
			tris[t * 3 + 2] = buf.getInt();
			adjacency[t][0] = buf.getInt();
			adjacency[t][1] = buf.getInt();
			adjacency[t][2] = buf.getInt();
		}

		// compute bounding box
		float[] bmin = { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
		float[] bmax = { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
		for (int i = 0; i < vertexCount; i++) {
			float x = verts[i * 3];
			float y = verts[i * 3 + 1];
			float z = verts[i * 3 + 2];
			bmin[0] = Math.min(bmin[0], x);
			bmin[1] = Math.min(bmin[1], y);
			bmin[2] = Math.min(bmin[2], z);
			bmax[0] = Math.max(bmax[0], x);
			bmax[1] = Math.max(bmax[1], y);
			bmax[2] = Math.max(bmax[2], z);
		}

		// convert Aion coordinate system (Z-up) to Detour (Y-up)
		// aion(x, y, z) -> detour(x, z, -y)
		float[] detourVerts = new float[vertexCount * 3];
		for (int i = 0; i < vertexCount; i++) {
			detourVerts[i * 3] = verts[i * 3];       // x -> x
			detourVerts[i * 3 + 1] = verts[i * 3 + 2]; // z -> y (detour up)
			detourVerts[i * 3 + 2] = -verts[i * 3 + 1]; // -y -> z
		}

		// recompute bounding box in Detour space
		float[] detourBmin = { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
		float[] detourBmax = { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
		for (int i = 0; i < vertexCount; i++) {
			float x = detourVerts[i * 3];
			float y = detourVerts[i * 3 + 1];
			float z = detourVerts[i * 3 + 2];
			detourBmin[0] = Math.min(detourBmin[0], x);
			detourBmin[1] = Math.min(detourBmin[1], y);
			detourBmin[2] = Math.min(detourBmin[2], z);
			detourBmax[0] = Math.max(detourBmax[0], x);
			detourBmax[1] = Math.max(detourBmax[1], y);
			detourBmax[2] = Math.max(detourBmax[2], z);
		}

		// build Detour NavMesh params
		NavMeshDataCreateParams params = new NavMeshDataCreateParams();
		params.verts = detourVerts;
		params.vertCount = vertexCount;
		params.polys = tris;
		params.polyAreas = new int[triangleCount];
		params.polyFlags = new int[triangleCount];
		params.polyCount = triangleCount;
		params.nvp = 3; // triangles (3 vertices per poly)
		params.bmin = detourBmin;
		params.bmax = detourBmax;
		params.cs = 0.3f;  // cell size
		params.ch = 0.2f;  // cell height
		params.walkableHeight = 2.0f;
		params.walkableRadius = 0.6f;
		params.walkableClimb = 0.9f;
		params.buildBvTree = true;

		// set area flags for all polygons to walkable
		for (int i = 0; i < triangleCount; i++) {
			params.polyAreas[i] = 1;  // POLYAREA_GROUND
			params.polyFlags[i] = 1;  // POLYFLAGS_WALK
		}

		MeshData meshData = NavMeshBuilder.createNavMeshData(params);
		if (meshData == null) {
			throw new IOException("NavMeshBuilder.createNavMeshData() returned null");
		}

		NavMesh navMesh = new NavMesh(meshData, params.nvp, 0);
		ByteBuffer outBuf = navMesh.saveTo();
		Files.write(outFile, toByteArray(outBuf));
	}

	private static byte[] toByteArray(ByteBuffer buf) {
		buf.flip();
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		return data;
	}
}
