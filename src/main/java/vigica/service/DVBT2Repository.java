package vigica.service;

import org.springframework.stereotype.Repository;

import vigica.model.DVBT2Service;

@Repository
public interface DVBT2Repository extends DVBRepository<DVBT2Service> {

	// public List<DVBT2Service> findByNameContainingIgnoreCase(String name);
}
