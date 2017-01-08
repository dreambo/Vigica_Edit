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
package vigica_edit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import vigica_edit.model.Service;
import vigica_edit.view.Error_Msg;

/**
 *
 * @author bnabi
 */
public class Service_BDD {
    
    static private Error_Msg error_msg = new Error_Msg();
    
    public Service_BDD(){
        
    }
    
    public List<Service> read_bdd () throws HibernateException {
        return read_bdd("FROM Service");
    }
    
    public List<Service> read_bdd (String sql) throws HibernateException {
        List<Service> services = new ArrayList<>();
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            Query q = session.createQuery(sql);

            for (Iterator<Service> it = q.iterate(); it.hasNext();) {
                Service result = it.next();
                Service service = new Service(result.getS_type(), result.getS_idx(), result.getS_name(), result.getS_nid(), result.getS_ppr(), result.getS_line(), result.getS_flag(), result.getS_new());
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
    
    public void save_bdd (Service service)  throws HibernateException {
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
    
    public void save_bdd (List<Service> services)  throws HibernateException {
        for(Service service : services){
            save_bdd(service);
        }
    }
    
    public void update_bdd (Service service)  throws HibernateException {
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
    
    public void delete_bdd (Service service) throws HibernateException {
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
}
