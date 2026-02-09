package com.example.nursemedication.controller;

import com.example.nursemedication.model.Medication;
import com.example.nursemedication.model.MedicationSchedule;
import com.example.nursemedication.model.Nurse;
import com.example.nursemedication.model.Patient;
import com.example.nursemedication.service.MedicationService;
import com.example.nursemedication.service.NurseService;
import com.example.nursemedication.service.PatientService;
import com.example.nursemedication.service.SchedulerService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/nurse")
public class NurseController {

    private final NurseService nurseService;
    private final PatientService patientService;
    private final MedicationService medicationService;
    private final SchedulerService schedulerService;

    public NurseController(NurseService nurseService, PatientService patientService,
            MedicationService medicationService, SchedulerService schedulerService) {
        this.nurseService = nurseService;
        this.patientService = patientService;
        this.medicationService = medicationService;
        this.schedulerService = schedulerService;
    }

    // 👤 Get nurse profile
    @GetMapping("/profile")
    public Nurse getProfile(HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }
        return nurseService.getNurseById(nurse.getId());
    }

    // 👤 Update nurse profile
    @PutMapping("/profile")
    public Nurse updateProfile(@RequestBody Nurse updatedNurse, HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }
        return nurseService.updateNurseProfile(nurse.getId(), updatedNurse);
    }

    // 👩‍⚕️ Get patients assigned to nurse's floor
    @GetMapping("/patients")
    public List<Patient> getPatientsByFloor(HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }
        return patientService.getPatientsByFloor(nurse.getFloor().getId());
    }

    // 👤 Nurse gets patient details
    @GetMapping("/patients/{patientId}")
    public Patient getPatientDetails(@PathVariable Long patientId, HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        Patient patient = patientService.getPatientById(patientId);

        if (!patient.getFloor().getId().equals(nurse.getFloor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this patient");
        }

        return patient;
    }

    // 💊 Get all medications for patients in nurse's floor
    @GetMapping("/medications")
    public List<Medication> getMedicationsForFloor(HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        Long floorId = nurse.getFloor().getId();
        return medicationService.getMedicationsByFloor(floorId);
    }

    // 💊 Get medications for a specific patient (only if on nurse's floor)
    @GetMapping("/patients/{patientId}/medications")
    public List<Medication> getMedicationsForPatient(@PathVariable Long patientId, HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        Patient patient = patientService.getPatientById(patientId);
        if (!patient.getFloor().getId().equals(nurse.getFloor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this patient's medications");
        }

        return medicationService.getMedicationsByPatient(patientId);
    }

    // ⏰ Get today's medication schedules for nurse's floor
    @GetMapping("/medication-schedules")
    public List<MedicationSchedule> getTodaysSchedules(HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        Long floorId = nurse.getFloor().getId();
        LocalDate today = LocalDate.now();

        return schedulerService.getAllSchedules().stream()
                .filter(s -> s.getPatient().getFloor().getId().equals(floorId))
                .filter(s -> s.getScheduledTime().toLocalDate().equals(today))
                .collect(Collectors.toList());
    }

    // ⏰ Get upcoming schedules (future today or later) for nurse's floor
    @GetMapping("/medication-schedules/upcoming")
    public List<MedicationSchedule> getUpcomingSchedules(HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        Long floorId = nurse.getFloor().getId();
        LocalDateTime now = LocalDateTime.now();

        return schedulerService.getAllSchedules().stream()
                .filter(s -> s.getPatient().getFloor().getId().equals(floorId))
                .filter(s -> s.getScheduledTime().isAfter(now) && s.getStatus() == MedicationSchedule.Status.PENDING)
                .collect(Collectors.toList());
    }

    // ⏰ Get missed schedules for nurse's floor
    @GetMapping("/medication-schedules/missed")
    public List<MedicationSchedule> getMissedSchedules(HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        Long floorId = nurse.getFloor().getId();
        LocalDateTime now = LocalDateTime.now();

        return schedulerService.getAllSchedules().stream()
                .filter(s -> s.getPatient().getFloor().getId().equals(floorId))
                .filter(s -> s.getStatus() == MedicationSchedule.Status.MISSED
                        || (s.getScheduledTime().isBefore(now) && s.getStatus() == MedicationSchedule.Status.PENDING))
                .collect(Collectors.toList());
    }

    // ✅ Mark medication as GIVEN
    @PutMapping("/medication-schedules/{scheduleId}/mark-given")
    public MedicationSchedule markGiven(@PathVariable Long scheduleId, HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        MedicationSchedule schedule = schedulerService.getScheduleById(scheduleId);

        // Ensure nurse is assigned to the same floor as the patient
        if (!schedule.getPatient().getFloor().getId().equals(nurse.getFloor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify schedule outside your floor");
        }

        schedule.setStatus(MedicationSchedule.Status.GIVEN);
        return schedulerService.saveSchedule(schedule);
    }

    // ✅ Mark medication as MISSED
    @PutMapping("/medication-schedules/{scheduleId}/mark-missed")
    public MedicationSchedule markMissed(@PathVariable Long scheduleId, HttpSession session) {
        Nurse nurse = (Nurse) session.getAttribute("USER");
        if (nurse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nurse not logged in");
        }

        MedicationSchedule schedule = schedulerService.getScheduleById(scheduleId);

        // Ensure nurse is assigned to the same floor as the patient
        if (!schedule.getPatient().getFloor().getId().equals(nurse.getFloor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify schedule outside your floor");
        }

        schedule.setStatus(MedicationSchedule.Status.MISSED);
        return schedulerService.saveSchedule(schedule);
    }
}
