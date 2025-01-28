package com.example.blog.config;

import com.example.blog.model.Role;
import com.example.blog.model.Room;
import com.example.blog.repository.RoleRepository;
import com.example.blog.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final RoomService roomService;
    @Autowired
    private RoleRepository roleRepository;

    public DataInitializer(RoomService roomService){
        this.roomService = roomService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!roleRepository.findByName("ROLE_USER").isPresent()) {
            roleRepository.save(Role.builder().name("ROLE_USER").build());
        }
        if (!roleRepository.findByName("ROLE_ADMIN").isPresent()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        }
        if(roomService.findAllRooms().isEmpty()){
            roomService.createRoom("sports", "Discuss all things sports");
            roomService.createRoom("technology", "Talk about the latest in tech");
            roomService.createRoom("finance", "Finance and investment discussions");
            roomService.createRoom("programming", "Share programming knowledge");
            roomService.createRoom("school", "School-related topics");
            roomService.createRoom("music", "Music discussions");
            roomService.createRoom("films", "Film and cinema talk");
        }
    }
}
