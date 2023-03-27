package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbRespondents;

public interface JavaMailSenderService {

    void sendMail(DbRespondents dbRespondents, String status);
}
