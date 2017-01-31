package vigica.service;

import java.util.Collection;
import java.util.List;

import vigica.model.DVBService;

public interface IDBService {

	public abstract List<DVBService> read_bdd();

	public abstract List<DVBService> read_bdd(String name);

	public abstract void save_bdd(DVBService service);

	public abstract void save_bdd(Collection<DVBService> services);

	public abstract void update_bdd(DVBService service);

	public abstract void delete_bdd(DVBService service);

}
