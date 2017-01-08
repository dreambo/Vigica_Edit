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

import java.net.URL;
import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import vigica_edit.view.Error_Msg;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author bnabi
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory;
    static private Error_Msg error_msg = new Error_Msg();
    
    static {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml) 
            // config file.
            URL myurl = Thread.currentThread().getContextClassLoader().getResource("/hibernate.cfg.xml");
            sessionFactory = new Configuration().configure(myurl).buildSessionFactory();
        } catch (Throwable e) {
            // Log the exception.
            error_msg.Error_diag(e.getCause().getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
