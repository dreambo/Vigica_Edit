package vigica.service;

import java.util.Collection;
import java.util.List;

import vigica.model.DVBService;

public interface IDVBDBService<T extends DVBService> {

	public abstract List<T> read_bdd();

	public abstract List<T> read_bdd(String name);

	public abstract void save_bdd(T service);

	public abstract void save_bdd(Collection<T> services);

	public abstract void update_bdd(T service);

	public abstract void delete_bdd(T service);
}
