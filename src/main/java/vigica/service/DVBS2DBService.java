package vigica.service;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vigica.model.DVBS2Service;

@Service
public class DVBS2DBService implements IDVBDBService<DVBS2Service> {

	@Autowired
    private DVBRepository<DVBS2Service> repository;

    @Override
	public List<DVBS2Service> read_bdd() {

    	return repository.findAll();
    }

    @Override
	public List<DVBS2Service> read_bdd(String name) {

        return repository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
	public void save_bdd(DVBS2Service service)  {

    	repository.save(service);
    }
    
    @Override
	public void save_bdd (Collection<DVBS2Service> services)  {

    	repository.save(services);
    }
    
    @Override
	public void update_bdd(DVBS2Service service)  {

    	repository.save(service);
    }
    
    @Override
	public void delete_bdd (DVBS2Service service) {

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
