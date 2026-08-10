package tech.masterfix.service;

import org.springframework.stereotype.Service;
import tech.masterfix.dto.ContactRequest;
import tech.masterfix.model.ContactMessage;
import tech.masterfix.repository.ContactMessageRepository;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    public ContactMessage saveMessage(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setFullName(request.getFullName());
        message.setPhone(request.getPhone());
        message.setEmail(request.getEmail());
        message.setMessage(request.getMessage());
        return contactMessageRepository.save(message);
    }
}
