package vigica.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vigica.model.DVBService;

@Service
public class DBService implements IDBService {

	@Autowired
    private DBRepository repository; // = BeanFactory.getBean(ServiceRepository.class);

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

/*
	@Bean
	public ServletRegistrationBean h2servletRegistration() {
	    ServletRegistrationBean registration = new ServletRegistrationBean(new WebServlet());
	    registration.addUrlMappings("/console/*");
	    return registration;
	}
*/
}
