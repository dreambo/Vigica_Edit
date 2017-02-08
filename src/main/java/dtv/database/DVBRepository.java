package dtv.database;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dtv.model.DVBChannel;

@Repository
public interface DVBRepository<T extends DVBChannel> extends JpaRepository<T, Integer> {

	public List<T> findByNameContainingIgnoreCase(String name);
}
