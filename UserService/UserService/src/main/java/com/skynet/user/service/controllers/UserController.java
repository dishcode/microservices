package com.skynet.user.service.controllers;

import com.skynet.user.service.entities.User;
import com.skynet.user.service.services.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    int retryCount = 1;

    @GetMapping("/{userId}")
//    @CircuitBreaker(name = "ratingHotelBreaker", fallbackMethod = "ratingHotelFallback")
//    @Retry(name = "ratingHotelService", fallbackMethod = "ratingHotelFallback")
    @RateLimiter(name = "userRateLimiter", fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getSingleUser(@PathVariable String userId) {
        logger.info("Retry count {}", retryCount);
        retryCount++;
        User savedUser = userService.getUser(userId);
        return ResponseEntity.ok(savedUser);
    }

    // Fallback for getSingleUser method using Circuit Breaker
    public ResponseEntity<User> ratingHotelFallback(String userId, Exception exception) {
        logger.info("Fallback is executed because service is down : {}", exception.getMessage());
        User user = User.builder()
                .userId("0")
                .name("dummy")
                .email("dummy@dummy.com")
                .about("This user is created as dummy user because service is down")
                .build();
        return new ResponseEntity<>(user, HttpStatus.TOO_MANY_REQUESTS);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> savedUsers = userService.getAllUser();
        return ResponseEntity.ok(savedUsers);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        String response = userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
