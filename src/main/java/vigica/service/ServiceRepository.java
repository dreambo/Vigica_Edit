package vigica.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vigica.model.Service;

@Repository
interface ServiceRepository extends JpaRepository<Service, Integer> {

	final static String SQL_1 = "select * FROM Service WHERE idx=:IDX AND type like %:TYPE% AND UPPER(name) like %:NAME% ORDER BY idx";

	@org.springframework.data.jpa.repository.Query(value=SQL_1, nativeQuery=true)
	public List<Service> getService(@Param("IDX") Integer idx, @Param("TYPE") String type, @Param("NAME") String name);
}
