package com.aionemu.commons.database.dao;

/**
 * Data Access Object (DAO) interface.
 *
 * Every abstract DAO base class implements {@link #getClassName()} to return
 * its own fully-qualified name, which serves as the lookup key in
 * {@link DAOManager}.
 *
 * @author SoulKeeper
 * @author Saelya
 */
public interface DAO {

    /**
     * Return the fully-qualified class name of the <em>abstract</em> DAO base
     * class.  This value is used as the key in {@link DAOManager#getDAO(Class)}.
     *
     * @return fully-qualified name of the abstract DAO class
     */
    String getClassName();
}
