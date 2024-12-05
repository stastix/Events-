package tn.esprit.eventsproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventServicesImplMockTest {

    @InjectMocks
    private EventServicesImpl eventServices;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddParticipant() {
        // Arrange
        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("John");

        when(participantRepository.save(participant)).thenReturn(participant);

        // Act
        Participant savedParticipant = eventServices.addParticipant(participant);

        // Assert
        assertNotNull(savedParticipant);
        assertEquals("John", savedParticipant.getNom());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipantWithId() {
        // Arrange
        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Tech Conference");

        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setEvents(new HashSet<>());

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        // Act
        Event updatedEvent = eventServices.addAffectEvenParticipant(event, 1);

        // Assert
        assertNotNull(updatedEvent);
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog() {
        // Arrange
        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Music Festival");
        event.setLogistics(new HashSet<>());

        Logistics logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setPrixUnit(100);
        logistics.setQuantite(5);

        when(eventRepository.findByDescription("Music Festival")).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        // Act
        Logistics savedLogistics = eventServices.addAffectLog(logistics, "Music Festival");

        // Assert
        assertNotNull(savedLogistics);
        assertTrue(event.getLogistics().contains(logistics));
        verify(eventRepository, times(1)).findByDescription("Music Festival");
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Event event = new Event();
        event.setIdEvent(1);
        event.setDateDebut(LocalDate.of(2024, 12, 5));

        Logistics logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setReserve(true);

        event.setLogistics(Set.of(logistics));

        when(eventRepository.findByDateDebutBetween(startDate, endDate)).thenReturn(List.of(event));

        // Act
        List<Logistics> logisticsList = eventServices.getLogisticsDates(startDate, endDate);

        // Assert
        assertNotNull(logisticsList);
        assertEquals(1, logisticsList.size());
        assertTrue(logisticsList.contains(logistics));
        verify(eventRepository, times(1)).findByDateDebutBetween(startDate, endDate);
    }

    @Test
    void testCalculCout() {
        // Arrange
        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Tech Meetup");
        event.setCout(0); // Initial cost

        Logistics logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setPrixUnit(50);
        logistics.setQuantite(10);
        logistics.setReserve(true);

        event.setLogistics(Set.of(logistics));

        // Mock the repository method to return the event
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(List.of(event));

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        eventServices.calculCout();

        // Assert
        assertEquals(500.0, event.getCout()); // 50 * 10
        verify(eventRepository, times(1)).save(event); // Ensure save() is called
        verify(eventRepository, times(1))
                .findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed",
                        Tache.ORGANISATEUR);
    }
}
