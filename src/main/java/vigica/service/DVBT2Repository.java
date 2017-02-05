package vigica.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vigica.model.DVBT2Service;

@Repository
public interface DVBT2Repository extends JpaRepository<DVBT2Service, Integer> {

	public List<DVBT2Service> findByNameContainingIgnoreCase(String name);
}
