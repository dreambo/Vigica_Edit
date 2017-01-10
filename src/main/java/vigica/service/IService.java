package vigica.service;

import java.util.List;

import org.hibernate.HibernateException;

import vigica.model.Service;

public interface IService {

	public abstract List<Service> read_bdd() throws HibernateException;

	public abstract List<Service> read_bdd(Integer idx, String type, String name);

	public abstract void save_bdd(Service service) throws HibernateException;

	public abstract void save_bdd(List<Service> services) throws HibernateException;

	public abstract void update_bdd(Service service) throws HibernateException;

	public abstract void delete_bdd(Service service) throws HibernateException;

	public abstract void truncate_bdd() throws HibernateException;

}
