package com.noon.quartz.repository;

import com.noon.quartz.model.QrtzAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuartzRepository extends JpaRepository<QrtzAudit, Long> {

}
