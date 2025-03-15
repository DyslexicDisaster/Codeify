package codeify.controllers;

import codeify.entities.User;
import codeify.entities.role;
import codeify.persistance.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    // Get all users
    @GetMapping("/get_all_users")
    public ResponseEntity<String> getAllUsers() {
        try{
            return ResponseEntity.ok(userRepositoryImpl.getAllUsers().toString());
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Add user
    @PostMapping("/add_user")
    public ResponseEntity<String> addUser(@RequestBody User user) {
        try{
            boolean added = userRepositoryImpl.register(user);
            if(added){
                return ResponseEntity.ok("User added successfully");
            } else {
                return ResponseEntity.badRequest().body("User not added");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Delete user
    @DeleteMapping("/delete_user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        try{
            boolean deleted = userRepositoryImpl.deleteUserById(id);
            if(deleted){
                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.badRequest().body("User not deleted");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Update user
    @PutMapping("/update_user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        try{
            boolean updated = userRepositoryImpl.updateUser(user);
            if(updated){
                return ResponseEntity.ok("User updated successfully");
            } else {
                return ResponseEntity.badRequest().body("User not updated");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Change user role
    @PutMapping("/change_role/{id}")
    public ResponseEntity<String> changeRole(@PathVariable int id, @RequestParam String role) {
        try{
            boolean updated = userRepositoryImpl.changeRole(id, codeify.entities.role.valueOf(role));
            if(updated){
                return ResponseEntity.ok("Role changed successfully");
            } else {
                return ResponseEntity.badRequest().body("Role not changed");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Reset user password
    @PutMapping("/reset_password/{id}")
    public ResponseEntity<String> resetPassword(@PathVariable int id, @RequestParam String password) {
        try{
            boolean updated = userRepositoryImpl.resetPassword(id, password);
            if(updated){
                return ResponseEntity.ok("Password reset successfully");
            } else {
                return ResponseEntity.badRequest().body("Password not reset");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
