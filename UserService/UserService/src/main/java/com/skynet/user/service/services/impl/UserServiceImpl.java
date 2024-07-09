package com.skynet.user.service.services.impl;

import com.skynet.user.service.entities.Hotel;
import com.skynet.user.service.entities.Rating;
import com.skynet.user.service.entities.User;
import com.skynet.user.service.exceptions.ResourceNotFoundException;
import com.skynet.user.service.external.services.HotelService;
import com.skynet.user.service.repositories.UserRepository;
import com.skynet.user.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HotelService hotelService;

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User saveUser(User user) {
        user.setUserId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        List<User> usersList = userRepository.findAll();
        for (User user : usersList) {
            Rating[] userRatingsArray = restTemplate.getForObject("http://RATING-SERVICE/ratings/users/" + user.getUserId(), Rating[].class);
            List<Rating> userRatings = Arrays.stream(userRatingsArray).toList();
            userRatings.forEach(rating -> {
//                ResponseEntity<Hotel> hotelResponseEntity = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/" + rating.getHotelId(), Hotel.class);
//                logger.info("Hotel status code {}", hotelResponseEntity.getStatusCode());
//                rating.setHotel(hotelResponseEntity.getBody());
                rating.setHotel(hotelService.getHotel(rating.getHotelId()));
            });
            user.setRatings(userRatings);
        }
        return usersList;
    }

    @Override
    public User getUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with given id is not found on server !! : " + userId));
        Rating[] userRatingsArray = restTemplate.getForObject("http://RATING-SERVICE/ratings/users/" + user.getUserId(), Rating[].class);
        List<Rating> userRatings = Arrays.stream(userRatingsArray).toList();
        userRatings.forEach(rating -> {
//            ResponseEntity<Hotel> hotelResponseEntity = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/" + rating.getHotelId(), Hotel.class);
//            logger.info("Hotel status code {}", hotelResponseEntity.getStatusCode());
//            rating.setHotel(hotelResponseEntity.getBody());
            rating.setHotel(hotelService.getHotel(rating.getHotelId()));
        });
        user.setRatings(userRatings);
        return user;
    }

    @Override
    public String deleteUser(String userId) {
        userRepository.deleteById(userId);
        return String.format("User with id %s has been deleted.", userId);
    }

    @Override
    public User updateUser(User user) {
        User oldUser = getUser(user.getUserId());
        oldUser.setAbout(user.getAbout());
        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());
        return userRepository.save(oldUser);
    }
}
