package vigica.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import vigica.model.Service;

@org.springframework.stereotype.Service
public class Service_BDD2 implements IService {

	@Autowired
    private ServiceRepository repository = BeanFactory.getBean(ServiceRepository.class);

    @Override
	public List<Service> read_bdd() {

    	return repository.findAll();
    }

    @Override
	public List<Service> read_bdd(Integer idx, String type, String name) {

        return repository.getService(idx, type, name);
    }
    
    @Override
	public void save_bdd(Service service)  {

    	repository.save(service);
    }
    
    @Override
	public void save_bdd (List<Service> services)  {

    	repository.save(services);
    }
    
    @Override
	public void update_bdd(Service service)  {

    	repository.save(service);
    }
    
    @Override
	public void delete_bdd (Service service) {

    	repository.delete(service);
    }
    
    @Override
	public void truncate_bdd() {

    	repository.deleteAll();
    }
}
