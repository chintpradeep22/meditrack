package com.example.nursemedication.controller;

import com.example.nursemedication.model.Medication;
import com.example.nursemedication.model.MedicationSchedule;
import com.example.nursemedication.model.Nurse;
import com.example.nursemedication.model.Patient;
import com.example.nursemedication.service.*;
import com.example.nursemedication.dto.MedicationScheduleDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final NurseService nurseService;
    private final PatientService patientService;
    private final MedicationService medicationService;
    private final SchedulerService scheduleService;

    public AdminController(NurseService nurseService, PatientService patientService,
            MedicationService medicationService, SchedulerService scheduleService) {
        this.nurseService = nurseService;
        this.patientService = patientService;
        this.medicationService = medicationService;
        this.scheduleService = scheduleService;
    }

    // 👩‍⚕️ Get all nurses
    @GetMapping("/nurses")
    public List<Nurse> getAllNurses(HttpSession session) {
        // ✅ Check if admin is logged in
        Object user = session.getAttribute("USER");
        if (user == null || !user.getClass().getSimpleName().equals("User")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }

        return nurseService.getAllNurses();
    }

    @PutMapping("/nurses/{nurseId}/assign-floor/{floorId}")
    public Nurse assignFloor(
            @PathVariable Long nurseId,
            @PathVariable Long floorId,
            HttpSession session) {
        // Only admin can do this
        Object user = session.getAttribute("USER");
        if (user == null || !user.getClass().getSimpleName().equals("User")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }

        return nurseService.assignFloorToNurse(nurseId, floorId);
    }

    // ✅ Get all nurses assigned to a floor
    @GetMapping("/floors/{floorId}/nurses")
    public List<Nurse> getNursesByFloor(
            @PathVariable Long floorId,
            HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null || !user.getClass().getSimpleName().equals("User")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }

        return nurseService.getNursesByFloor(floorId);
    }

    @GetMapping("/patients")
    public List<Patient> getAllPatients(HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null || !user.getClass().getSimpleName().equals("User")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }

        return patientService.getAllPatients();
    }

    @PutMapping("/patients/{patientId}/assign-floor/{floorId}")
    public Patient assignPatientToFloor(@PathVariable Long patientId, @PathVariable Long floorId, HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }

        return patientService.assignPatientToFloor(patientId, floorId);
    }

    // ✅ Get all patients in a floor
    @GetMapping("/floors/{floorId}/patients")
    public List<Patient> getPatientsByFloor(@PathVariable Long floorId, HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }

        return patientService.getPatientsByFloor(floorId);
    }

    @PostMapping("/medications/{patientId}")
    public Medication addMedication(@PathVariable Long patientId,
            @RequestBody Medication medication, HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }
        return medicationService.addMedication(patientId, medication);
    }

    @GetMapping("/medications")
    public List<Medication> getAllMedications(HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }
        return medicationService.getAllMedications();
    }

    @GetMapping("/patients/{patientId}/medications")
    public List<Medication> getMedicationsByPatient(@PathVariable Long patientId, HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }
        return medicationService.getMedicationsByPatient(patientId);
    }

    @PostMapping("/medication-schedules")
    public MedicationSchedule createSchedule(@RequestBody MedicationScheduleDTO dto, HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }
        return scheduleService.createScheduleFromDto(dto);
    }

    // Get all schedules
    @GetMapping("/medication-schedules")
    public List<MedicationSchedule> getAllSchedules(HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }
        return scheduleService.getAllSchedules();
    }

    // Update schedule
    @PutMapping("/medication-schedules/{scheduleId}")
    public MedicationSchedule updateSchedule(@PathVariable Long scheduleId,
            @RequestBody MedicationScheduleDTO dto, HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }
        return scheduleService.updateScheduleFromDto(scheduleId, dto);
    }

    // Delete schedule
    @DeleteMapping("/medication-schedules/{scheduleId}")
    public String deleteSchedule(@PathVariable Long scheduleId, HttpSession session) {
        Object user = session.getAttribute("USER");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not logged in");
        }
        scheduleService.deleteSchedule(scheduleId);
        return "Schedule deleted successfully";
    }
}
