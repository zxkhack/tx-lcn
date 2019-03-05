package com.codingapi.txlcn.tc.core.mode.txc;

import com.codingapi.txlcn.tc.core.context.BranchContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * Description: Mapping all dataSource for this mod.
 * Date: 19-3-4 下午4:58
 *
 * @author ujued
 */
@Component
public class DataSourceMapping implements CommandLineRunner {

    private final List<DataSource> dataSources;

    private final BranchContext branchContext;

    @Autowired
    public DataSourceMapping(BranchContext branchContext, List<DataSource> dataSources) {
        this.branchContext = branchContext;
        this.dataSources = dataSources;
    }

    @Override
    public void run(String... args) {
        branchContext.mapDataSources(dataSources);
    }
}
