package com.devsuperior.dscatalog.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.tests.Factory;
import com.devsuperior.dscatalog.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objMapper;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;
	
	private String username, password, bearerToken;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
		
		username = "maria@gmail.com";
		password = "123456";
		
		bearerToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
	}
	
	@Test
	public void findAllShouldReturnSortedPageWhenSortByName() throws Exception {
		
		mockMvc.perform(get("/products?page=0&size=12&sort=name,asc")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(countTotalProducts))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"))
				.andExpect(jsonPath("$.content[1].name").value("PC Gamer"))
				.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		
		ProductDTO productDTO = Factory.createProductDTO();
		String jsonBody = objMapper.writeValueAsString(productDTO);
		
		String expectedName = productDTO.getName();
		String expectedDescription = productDTO.getDescription();
		double expectedPrice = productDTO.getPrice();
		
		mockMvc.perform(put("/products/{id}", existingId)
				.header("Authorization", "Bearer " + bearerToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value(expectedName))
				.andExpect(jsonPath("$.description").value(expectedDescription))
				.andExpect(jsonPath("$.price").value(expectedPrice));
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		
		ProductDTO productDTO = Factory.createProductDTO();
		String jsonBody = objMapper.writeValueAsString(productDTO);
		
		mockMvc.perform(put("/products/{id}", nonExistingId)
				.header("Authorization", "Bearer " + bearerToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}	
}
