/*
 * Copyright (C) 2016 bnabi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vigica.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import vigica.model.DVBService;
import vigica.tools.HibernateUtil;
import vigica.view.Error_Msg;

/**
 *
 * @author bnabi
 */
public class Service_BDD implements IService {

    static private Error_Msg error_msg = new Error_Msg();

    /* (non-Javadoc)
	 * @see vigica.IService#read_bdd()
	 */
    @Override
	public List<DVBService> read_bdd() throws HibernateException {
        return read_bdd(null, null, null);
    }
    
    /* (non-Javadoc)
	 * @see vigica.IService#read_bdd(java.lang.String)
	 */
    @Override
	public List<DVBService> read_bdd(Integer idx, String type, String name) throws HibernateException {

    	List<DVBService> services = new ArrayList<>();

        String sql = "FROM Service WHERE 1=1 ";
        if (idx != null) {
            sql += " AND IDX = " + idx;
        }

        if (type != null && !type.isEmpty()) {
            sql += " AND TYPE like '" + type + "'";
        }

        if (name != null && !name.isEmpty()) {
            sql += " AND UPPER(NAME) like UPPER('" + name + "')";
        }

        sql += " ORDER BY IDX";

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            Query q = session.createQuery(sql);

            for (Iterator<DVBService> it = q.iterate(); it.hasNext();) {
                DVBService result = it.next();
                DVBService service = new DVBService(result.getType(), result.getIdx(), result.getName(), result.getNid(), result.getPpr(), result.getLine(), result.getFlag(), result.getNeew());
                services.add(service);
            }
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new HibernateException(e.getCause().getMessage());
        }finally {
            // close the session
            session.close();
        }
        
        return services;
    }
    
    /* (non-Javadoc)
	 * @see vigica.IService#save_bdd(vigica.model.Service)
	 */
    @Override
	public void save_bdd (DVBService service)  throws HibernateException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            session.save(service);
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new HibernateException(e.getCause().getMessage());
        }finally {
            // close the session
            session.close();
        }
    }
    
    /* (non-Javadoc)
	 * @see vigica.IService#save_bdd(java.util.List)
	 */
    @Override
	public void save_bdd (List<DVBService> services)  throws HibernateException {
        for(DVBService service : services){
            save_bdd(service);
        }
    }
    
    /* (non-Javadoc)
	 * @see vigica.IService#update_bdd(vigica.model.Service)
	 */
    @Override
	public void update_bdd (DVBService service)  throws HibernateException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            session.update(service);
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new HibernateException(e.getCause().getMessage());
        }finally {
            // close the session
            session.close();
        }
    }
    
    /* (non-Javadoc)
	 * @see vigica.IService#delete_bdd(vigica.model.Service)
	 */
    @Override
	public void delete_bdd (DVBService service) throws HibernateException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            session.delete(service);
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new HibernateException(e.getCause().getMessage());
        }finally {
            // close the session
            session.close();
        }
    }
    
    /* (non-Javadoc)
	 * @see vigica.IService#truncate_bdd()
	 */
    @Override
	public void truncate_bdd () throws HibernateException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            
            Query q = session.createSQLQuery("TRUNCATE TABLE Service");
            q.executeUpdate();
            
            tx.commit();
        }catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new HibernateException(e.getCause().getMessage());
        }finally {
            // close the session
            session.close();
        }
    }

	@Override
	public List<DVBService> read_bdd(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
