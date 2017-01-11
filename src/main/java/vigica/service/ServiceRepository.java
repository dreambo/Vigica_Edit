package vigica.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vigica.model.DVBService;

@Repository
interface ServiceRepository extends JpaRepository<DVBService, Integer> {

	public List<DVBService> findByNameContainingIgnoreCase(String name);
}
