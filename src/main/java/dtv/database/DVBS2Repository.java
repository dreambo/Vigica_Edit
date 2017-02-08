package dtv.database;

import org.springframework.stereotype.Repository;

import dtv.model.DVBS2Channel;

@Repository
public interface DVBS2Repository extends DVBRepository<DVBS2Channel> {

	// public List<DVBS2Service> findByNameContainingIgnoreCase(String name);
}
