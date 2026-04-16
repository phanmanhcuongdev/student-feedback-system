USE [SURVEY_SYSTEM_DEV];
GO

SET IDENTITY_INSERT [dbo].[Department] ON;
INSERT INTO [dbo].[Department] ([dept_id], [name]) VALUES
    (1, N'Computer Science'),
    (2, N'Information Systems');
SET IDENTITY_INSERT [dbo].[Department] OFF;
GO

SET IDENTITY_INSERT [dbo].[User] ON;
INSERT INTO [dbo].[User] ([user_id], [email], [pass_word], [role], [verify]) VALUES
    (1, N'admin@university.edu', N'$2a$10$lk.RCUFC0mwBCuqtMWPjieICJ/rgZ1LD/ZWBOrxN8/leR.VbTG5xy', N'ADMIN', 1),
    (2, N'teacher@university.edu', N'$2a$10$NAUl9QQgFCEW/iv/87jDKur7/3MfgOcepdc.cXXE0P0ZfrpxLxhMy', N'TEACHER', 1),
    (3, N'student.active@university.edu', N'$2a$10$4Tf.uaQGQdw2RIDZzkPSpep4LERq6jo06m1Q4w7EkBIivrZarnGlO', N'STUDENT', 1),
    (4, N'student.verified@university.edu', N'$2a$10$4Tf.uaQGQdw2RIDZzkPSpep4LERq6jo06m1Q4w7EkBIivrZarnGlO', N'STUDENT', 1),
    (5, N'student.pending@university.edu', N'$2a$10$4Tf.uaQGQdw2RIDZzkPSpep4LERq6jo06m1Q4w7EkBIivrZarnGlO', N'STUDENT', 1),
    (6, N'student.unverified@university.edu', N'$2a$10$4Tf.uaQGQdw2RIDZzkPSpep4LERq6jo06m1Q4w7EkBIivrZarnGlO', N'STUDENT', 0),
    (7, N'student.rejected@university.edu', N'$2a$10$4Tf.uaQGQdw2RIDZzkPSpep4LERq6jo06m1Q4w7EkBIivrZarnGlO', N'STUDENT', 1),
    (8, N'student.active2@university.edu', N'$2a$10$4Tf.uaQGQdw2RIDZzkPSpep4LERq6jo06m1Q4w7EkBIivrZarnGlO', N'STUDENT', 1);
SET IDENTITY_INSERT [dbo].[User] OFF;
GO

INSERT INTO [dbo].[Admin] ([user_id], [name]) VALUES
    (1, N'System Admin');
GO

INSERT INTO [dbo].[Teacher] ([user_id], [name], [teacher_code], [dept_id]) VALUES
    (2, N'Lecturer Demo', N'T0001', 1);
GO

INSERT INTO [dbo].[Student] (
    [user_id],
    [name],
    [student_code],
    [dept_id],
    [status],
    [student_card_img],
    [national_id_img],
    [review_reason],
    [review_notes],
    [reviewed_by_user_id],
    [reviewed_at],
    [resubmission_count]
) VALUES
    (3, N'Active Student', N'S0001', 1, N'ACTIVE', N'/docs/student-card-s0001.png', N'/docs/national-id-s0001.png', NULL, NULL, NULL, NULL, 0),
    (4, N'Email Verified Student', N'S0002', 1, N'EMAIL_VERIFIED', NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (5, N'Pending Approval Student', N'S0003', 2, N'PENDING', N'/docs/student-card-s0003.png', N'/docs/national-id-s0003.png', NULL, NULL, NULL, NULL, 0),
    (6, N'Unverified Student', N'S0004', 2, N'EMAIL_UNVERIFIED', NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (7, N'Rejected Student', N'S0005', 1, N'REJECTED', N'/docs/student-card-s0005-blurry.png', N'/docs/national-id-s0005-cutoff.png', N'Document mismatch', N'Please upload a clearer student card and a complete national ID image.', 1, DATEADD(DAY, -1, GETDATE()), 1),
    (8, N'Active Student Two', N'S0006', 2, N'ACTIVE', N'/docs/student-card-s0006.png', N'/docs/national-id-s0006.png', NULL, NULL, NULL, NULL, 0);
GO

SET IDENTITY_INSERT [dbo].[student_token] ON;
INSERT INTO [dbo].[student_token] ([id], [student_id], [token], [expired_at], [created_at], [delete_flg]) VALUES
    (1, 4, N'verify-token-email-verified', DATEADD(DAY, 1, GETDATE()), GETDATE(), 1),
    (2, 6, N'verify-token-email-unverified', DATEADD(DAY, 1, GETDATE()), GETDATE(), 0);
SET IDENTITY_INSERT [dbo].[student_token] OFF;
GO

SET IDENTITY_INSERT [dbo].[Password_Reset_Token] ON;
INSERT INTO [dbo].[Password_Reset_Token] ([id], [user_id], [token_hash], [expired_at], [created_at], [used_at]) VALUES
    (1, 3, N'8fefe692f690a3173176ecdff4318225afaeb97fdd6f60c866ed823d59221665', DATEADD(MINUTE, 30, GETDATE()), GETDATE(), NULL);
SET IDENTITY_INSERT [dbo].[Password_Reset_Token] OFF;
GO

SET IDENTITY_INSERT [dbo].[Survey] ON;
INSERT INTO [dbo].[Survey] ([survey_id], [title], [description], [start_date], [end_date], [lifecycle_state], [hidden], [created_by]) VALUES
    (1, N'Midterm Teaching Feedback', N'Collect student feedback for the midterm teaching period.', DATEADD(DAY, -3, GETDATE()), DATEADD(DAY, 7, GETDATE()), N'PUBLISHED', 0, 1),
    (2, N'Lab Session Feedback', N'Give feedback on the current lab sessions.', DATEADD(DAY, -1, GETDATE()), DATEADD(HOUR, 18, GETDATE()), N'PUBLISHED', 0, 1),
    (3, N'Final Course Feedback', N'This survey opens soon for final course feedback.', DATEADD(DAY, 2, GETDATE()), DATEADD(DAY, 9, GETDATE()), N'PUBLISHED', 0, 1),
    (4, N'Workshop Feedback', N'This workshop survey recently closed.', DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -1, GETDATE()), N'CLOSED', 0, 1),
    (5, N'Orientation Services Pulse Check', N'Draft survey for onboarding and orientation support feedback.', DATEADD(DAY, 5, GETDATE()), DATEADD(DAY, 12, GETDATE()), N'DRAFT', 0, 1);
SET IDENTITY_INSERT [dbo].[Survey] OFF;
GO

SET IDENTITY_INSERT [dbo].[Survey_Assignment] ON;
INSERT INTO [dbo].[Survey_Assignment] ([id], [survey_id], [evaluator_type], [evaluator_value], [subject_type], [subject_value]) VALUES
    (1, 1, N'STUDENT', NULL, N'ALL', NULL),
    (2, 2, N'STUDENT', NULL, N'ALL', NULL),
    (3, 3, N'STUDENT', NULL, N'DEPARTMENT', 1),
    (4, 4, N'STUDENT', NULL, N'ALL', NULL),
    (5, 5, N'STUDENT', NULL, N'DEPARTMENT', 2);
SET IDENTITY_INSERT [dbo].[Survey_Assignment] OFF;
GO

SET IDENTITY_INSERT [dbo].[Question] ON;
INSERT INTO [dbo].[Question] ([question_id], [survey_id], [content], [type]) VALUES
    (1, 1, N'Rate the lecturer''s teaching clarity.', N'RATING'),
    (2, 1, N'Share one suggestion for improvement.', N'TEXT'),
    (3, 2, N'Rate the usefulness of the lab sessions.', N'RATING'),
    (4, 2, N'What should be improved in the labs?', N'TEXT'),
    (5, 3, N'How prepared do you feel for the final review?', N'RATING'),
    (6, 3, N'What topics need more revision?', N'TEXT'),
    (7, 4, N'Rate the workshop content.', N'RATING'),
    (8, 4, N'Share one comment about the workshop.', N'TEXT'),
    (9, 5, N'How clear was the onboarding guidance?', N'RATING'),
    (10, 5, N'What should be improved before this survey is published?', N'TEXT');
SET IDENTITY_INSERT [dbo].[Question] OFF;
GO

SET IDENTITY_INSERT [dbo].[Survey_Recipient] ON;
INSERT INTO [dbo].[Survey_Recipient] ([recipient_id], [survey_id], [student_id], [assigned_at], [opened_at], [submitted_at]) VALUES
    (1, 1, 3, DATEADD(DAY, -3, GETDATE()), DATEADD(DAY, -2, GETDATE()), DATEADD(DAY, -1, GETDATE())),
    (2, 1, 8, DATEADD(DAY, -3, GETDATE()), DATEADD(HOUR, -12, GETDATE()), NULL),
    (3, 2, 3, DATEADD(DAY, -1, GETDATE()), NULL, NULL),
    (4, 2, 8, DATEADD(DAY, -1, GETDATE()), NULL, NULL),
    (5, 3, 3, DATEADD(HOUR, -1, GETDATE()), NULL, NULL),
    (6, 4, 3, DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -4, GETDATE()), NULL),
    (7, 4, 8, DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -3, GETDATE()), NULL);
SET IDENTITY_INSERT [dbo].[Survey_Recipient] OFF;
GO

SET IDENTITY_INSERT [dbo].[Survey_Response] ON;
INSERT INTO [dbo].[Survey_Response] ([response_id], [student_id], [survey_id], [teacher_id], [submitted_at]) VALUES
    (1, 3, 1, NULL, DATEADD(DAY, -1, GETDATE()));
SET IDENTITY_INSERT [dbo].[Survey_Response] OFF;
GO

SET IDENTITY_INSERT [dbo].[Response_Detail] ON;
INSERT INTO [dbo].[Response_Detail] ([id], [response_id], [question_id], [rating], [comment]) VALUES
    (1, 1, 1, 4, NULL),
    (2, 1, 2, NULL, N'More worked examples before labs would help.');
SET IDENTITY_INSERT [dbo].[Response_Detail] OFF;
GO

SET IDENTITY_INSERT [dbo].[Feedback] ON;
INSERT INTO [dbo].[Feedback] ([feedback_id], [student_id], [title], [content], [created_at]) VALUES
    (1, 3, N'Navigation suggestion', N'It would help if the survey list clearly separated active and closed surveys on mobile.', DATEADD(DAY, -2, GETDATE())),
    (2, 3, N'Result page wording', N'The status labels are clear overall, but some helper text could be simpler for students.', DATEADD(HOUR, -8, GETDATE()));
SET IDENTITY_INSERT [dbo].[Feedback] OFF;
GO

SET IDENTITY_INSERT [dbo].[Feedback_Response] ON;
INSERT INTO [dbo].[Feedback_Response] ([response_id], [feedback_id], [responder_user_id], [content], [created_at]) VALUES
    (1, 1, 1, N'Thanks for the suggestion. We will separate active and closed survey cards in the next UI pass.', DATEADD(DAY, -1, GETDATE())),
    (2, 2, 2, N'We will simplify the helper text on the result page so the status language is easier to scan.', DATEADD(HOUR, -3, GETDATE()));
SET IDENTITY_INSERT [dbo].[Feedback_Response] OFF;
GO
