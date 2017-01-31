package vigica.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import vigica.model.DVBService;

@org.springframework.stereotype.Service
public class Service implements IService {

	@Autowired
    private ServiceRepository repository; // = BeanFactory.getBean(ServiceRepository.class);

    @Override
	public List<DVBService> read_bdd() {

    	return repository.findAll();
    }

    @Override
	public List<DVBService> read_bdd(String name) {

        return repository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
	public void save_bdd(DVBService service)  {

    	repository.save(service);
    }
    
    @Override
	public void save_bdd (List<DVBService> services)  {

    	repository.save(services);
    }
    
    @Override
	public void update_bdd(DVBService service)  {

    	repository.save(service);
    }
    
    @Override
	public void delete_bdd (DVBService service) {

    	repository.delete(service);
    }
}
