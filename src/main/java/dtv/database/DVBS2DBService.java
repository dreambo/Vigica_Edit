package dtv.database;

import java.util.Collection;
import java.util.List;

import org.h2.server.web.WebServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import dtv.model.DVBS2Channel;

@Service
public class DVBS2DBService implements DVBDBService<DVBS2Channel> {

	@Autowired
    private DVBS2Repository repository;

    @Override
	public List<DVBS2Channel> read_bdd() {

    	return repository.findAll();
    }

    @Override
	public List<DVBS2Channel> read_bdd(String name) {

        return repository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
	public void save_bdd(DVBS2Channel service) {

    	repository.save(service);
    }
    
    @Override
	public void save_bdd (Collection<DVBS2Channel> services) {

    	repository.save(services);
    }
    
    @Override
	public void update_bdd(DVBS2Channel service) {

    	repository.save(service);
    }
    
    @Override
	public void delete_bdd(DVBS2Channel service) {

    	repository.delete(service);
    }

    @Override
	public void deleteAll() {

    	repository.deleteAll();
    }

	@Bean
	public ServletRegistrationBean h2servletRegistration() {
	    ServletRegistrationBean registration = new ServletRegistrationBean(new WebServlet());
	    registration.addUrlMappings("/console/*");
	    return registration;
	}
}