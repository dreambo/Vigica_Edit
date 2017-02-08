package dtv.database;

import org.springframework.stereotype.Repository;

import dtv.model.DVBT2Channel;

@Repository
public interface DVBT2Repository extends DVBRepository<DVBT2Channel> {

	// public List<DVBT2Service> findByNameContainingIgnoreCase(String name);
}
