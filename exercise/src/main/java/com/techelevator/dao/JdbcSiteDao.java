package com.techelevator.dao;

import com.techelevator.model.Site;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class   JdbcSiteDao implements SiteDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcSiteDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Site> getSitesThatAllowRVs(int parkId) {
        List<Site> sites = new ArrayList();
        String sql = "SELECT site_id, campground_id, site_number, max_occupancy, accessible, max_rv_length, utilities\n" +
                "FROM site\n" +
                "WHERE max_rv_length != 0;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()){
            Site site = mapRowToSite(results);
            sites.add(site);
        }
        return sites;
    }

    @Override
    public List<Site> getCurrentlyAvailableSites(int parkId, LocalDate toDate, LocalDate fromDate) {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT site_id, site.campground_id, site_number, max_occupancy, accessible, max_rv_length, utilities " +
                "FROM site " +
                "JOIN campground ON campground.campground_id = site.campground_id " +
                "WHERE campground.park_id = ? AND site_id NOT IN " +
                "(SELECT DISTINCT site_id FROM reservation WHERE (from_date, to_date) OVERLAPS (?, ?));";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, parkId, fromDate, toDate);

        while (results.next()) {
            Site site = mapRowToSite(results);
            sites.add(site);
        }
        return sites;
    }

    private Site mapRowToSite(SqlRowSet results) {
        Site site = new Site();
        site.setSiteId(results.getInt("site_id"));
        site.setCampgroundId(results.getInt("campground_id"));
        site.setSiteNumber(results.getInt("site_number"));
        site.setMaxOccupancy(results.getInt("max_occupancy"));
        site.setAccessible(results.getBoolean("accessible"));
        site.setMaxRvLength(results.getInt("max_rv_length"));
        site.setUtilities(results.getBoolean("utilities"));
        return site;
    }
}
