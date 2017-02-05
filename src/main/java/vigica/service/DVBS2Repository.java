package vigica.service;

import org.springframework.stereotype.Repository;

import vigica.model.DVBS2Service;

@Repository
public interface DVBS2Repository extends DVBRepository<DVBS2Service> {

	// public List<DVBS2Service> findByNameContainingIgnoreCase(String name);
}
