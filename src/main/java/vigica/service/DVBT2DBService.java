package vigica.service;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vigica.model.DVBT2Service;

@Service
public class DVBT2DBService implements DVBDBService<DVBT2Service> {

	@Autowired
    private DVBT2Repository repository;

    @Override
	public List<DVBT2Service> read_bdd() {

    	return repository.findAll();
    }

    @Override
	public List<DVBT2Service> read_bdd(String name) {

        return repository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
	public void save_bdd(DVBT2Service service)  {

    	repository.save(service);
    }
    
    @Override
	public void save_bdd(Collection<DVBT2Service> services)  {

    	repository.save(services);
    }

    @Override
	public void update_bdd(DVBT2Service service)  {

    	repository.save(service);
    }
    
    @Override
	public void delete_bdd(DVBT2Service service) {

    	repository.delete(service);
    }

    @Override
	public void deleteAll() {

    	repository.deleteAll();
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
