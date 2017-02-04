package vigica.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vigica.model.DVBService;

@Repository
public interface DVBRepository<T extends DVBService> extends JpaRepository<T, Integer> {

	public List<T> findByNameContainingIgnoreCase(String name);
}
