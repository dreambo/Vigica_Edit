package vigica.service;

import java.util.List;

import org.hibernate.HibernateException;

import vigica.model.DVBService;

public interface IService {

	public abstract List<DVBService> read_bdd() throws HibernateException;

	public abstract List<DVBService> read_bdd(Integer idx, String type, String name);

	public abstract List<DVBService> read_bdd(String name);

	public abstract void save_bdd(DVBService service) throws HibernateException;

	public abstract void save_bdd(List<DVBService> services) throws HibernateException;

	public abstract void update_bdd(DVBService service) throws HibernateException;

	public abstract void delete_bdd(DVBService service) throws HibernateException;

	public abstract void truncate_bdd() throws HibernateException;

}
