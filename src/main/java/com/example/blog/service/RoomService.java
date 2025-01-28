package com.example.blog.service;

import com.example.blog.model.Room;
import com.example.blog.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository){
        this.roomRepository = roomRepository;
    }

    public List<Room> findAllRooms(){
        return roomRepository.findAll();
    }

    public Room createRoom(String name, String description){
        Room room = Room.builder()
                .name(name)
                .description(description)
                .build();
        return roomRepository.save(room);
    }

    public Optional<Room> findByName(String name){
        return roomRepository.findByName(name);
    }
}
