package com.example.blog.controller;

import com.example.blog.model.Room;
import com.example.blog.service.RoomService;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ChatPageController {

    private final RoomService roomService;
    private final UserRepository userRepository;

    @Autowired
    public ChatPageController(RoomService roomService, UserRepository userRepository) {
        this.roomService = roomService;
        this.userRepository = userRepository;
    }

    @GetMapping("/chat/global")
    public String globalChat(Model model, Authentication authentication) {
        addDisplayNameToModel(model, authentication);
        return "global_chat";
    }

    @GetMapping("/chat/room/{roomName}")
    public String roomChat(@PathVariable String roomName, Model model, Authentication authentication) {
        Room room = roomService.findByName(roomName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room name: " + roomName));
        model.addAttribute("roomName", room.getName());
        addDisplayNameToModel(model, authentication);
        return "room_chat";
    }

    @GetMapping("/chat/private/{username}")
    public String privateChat(@PathVariable String username, Model model, Authentication authentication) {
        model.addAttribute("recipientName", username);
        model.addAttribute("recipientUsername", username);
        // Add displayName to the model
        addDisplayNameToModel(model, authentication);
        return "private_chat";
    }

    @GetMapping("/chat")
    public String chatHome(Model model, Authentication authentication) {
        model.addAttribute("rooms", roomService.findAllRooms());
        // Add displayName to the model
        addDisplayNameToModel(model, authentication);
        return "chat_home";
    }

    private void addDisplayNameToModel(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                String displayName = user.getUserProfile() != null ?
                        user.getUserProfile().getDisplayName() : "Anonymous";
                model.addAttribute("displayName", displayName);
            });
        } else {
            model.addAttribute("displayName", "Anonymous");
        }
    }
}