package vigica.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vigica.model.DVBS2Service;

@Repository
public interface DVBS2Repository extends JpaRepository<DVBS2Service, Integer> {

	public List<DVBS2Service> findByNameContainingIgnoreCase(String name);
}
