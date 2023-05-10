package com.mobileplus.dummytriluc.di

import com.mobileplus.dummytriluc.ui.login.main.LoginViewModel
import com.mobileplus.dummytriluc.ui.login.password.PasswordViewModel
import com.mobileplus.dummytriluc.ui.login.register.RegisterViewModel
import com.mobileplus.dummytriluc.ui.login.signin.SignInViewModel
import com.mobileplus.dummytriluc.ui.main.MainViewModel
import com.mobileplus.dummytriluc.ui.main.challenge.ChallengeViewModel
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailViewModel
import com.mobileplus.dummytriluc.ui.main.challenge.more.ChallengeMoreViewModel
import com.mobileplus.dummytriluc.ui.main.chat.chatmessage.ChatMessageViewModel
import com.mobileplus.dummytriluc.ui.main.chat.chatroom.ChatRoomViewModel
import com.mobileplus.dummytriluc.ui.main.coach.CoachMainViewModel
import com.mobileplus.dummytriluc.ui.main.coach.assign_exercise.CoachAssignExerciseViewModel
import com.mobileplus.dummytriluc.ui.main.coach.assigned_exercise.AssignedExerciseViewModel
import com.mobileplus.dummytriluc.ui.main.coach.create_course.CoachCreateCourseViewModel
import com.mobileplus.dummytriluc.ui.main.coach.group.CoachGroupViewModel
import com.mobileplus.dummytriluc.ui.main.coach.disciple.list.DiscipleListViewModel
import com.mobileplus.dummytriluc.ui.main.coach.disciple.waiting.DiscipleWaitingViewModel
import com.mobileplus.dummytriluc.ui.main.coach.draft.CoachDraftViewModel
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.lesson.CoachGroupDetailLessonViewModel
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.member.CoachGroupDetailMemberViewModel
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.message.CoachGroupDetailMessageViewModel
import com.mobileplus.dummytriluc.ui.main.coach.my_practice.CoachPracticeViewModel
import com.mobileplus.dummytriluc.ui.main.coach.register.mainregister.CoachRegisterViewModel
import com.mobileplus.dummytriluc.ui.main.coach.save_draft.CoachSaveDraftViewModel
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionViewModel
import com.mobileplus.dummytriluc.ui.main.coach.session.folder.CoachSessionChooseFolderViewModel
import com.mobileplus.dummytriluc.ui.main.coach.session.list_old.CoachSessionListOldViewModel
import com.mobileplus.dummytriluc.ui.main.coach.session.saved_list.CoachSessionSavedListViewModel
import com.mobileplus.dummytriluc.ui.main.editor_exercise.add_more_video.AddMoreVideoViewModel
import com.mobileplus.dummytriluc.ui.main.home.HomeViewModel
import com.mobileplus.dummytriluc.ui.main.news.NewsViewModel
import com.mobileplus.dummytriluc.ui.main.news.detail.NewsDetailViewModel
import com.mobileplus.dummytriluc.ui.main.notification.NotificationViewModel
import com.mobileplus.dummytriluc.ui.main.notification.detail.NotificationDetailViewModel
import com.mobileplus.dummytriluc.ui.main.power.PowerViewModel
import com.mobileplus.dummytriluc.ui.main.practice.PracticeMainViewModel
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailViewModel
import com.mobileplus.dummytriluc.ui.main.practice.filter.PracticeFilterViewModel
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderViewModel
import com.mobileplus.dummytriluc.ui.main.practice.test.PracticeTestViewModel
import com.mobileplus.dummytriluc.ui.main.punch.PunchViewModel
import com.mobileplus.dummytriluc.ui.main.ranking.RankingViewModel
import com.mobileplus.dummytriluc.ui.main.user.UserInfoViewModel
import com.mobileplus.dummytriluc.ui.main.editor_exercise.editor.EditVideoViewModel
import com.mobileplus.dummytriluc.ui.splash.SplashViewModel
import com.mobileplus.dummytriluc.ui.video.record.VideoRecordViewModel
import com.mobileplus.dummytriluc.ui.video.result.VideoResultViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModule: Module = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { SignInViewModel(get(), get(), get()) }
    viewModel { RegisterViewModel(get(), get(), get()) }
    viewModel { SplashViewModel(get(), get()) }
    viewModel { PasswordViewModel(get(), get()) }
    viewModel { PracticeMainViewModel(get(), get(), get()) }
    viewModel { NewsViewModel(get(), get(), get()) }
    viewModel { PracticeFilterViewModel(get(), get(), get()) }
    viewModel { PracticeDetailViewModel(get(), get(), get()) }
    viewModel { CoachMainViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { PracticeTestViewModel(get(), get(), get()) }
    viewModel { UserInfoViewModel(get(), get(), get()) }
    viewModel { PunchViewModel(get(), get(), get()) }
    viewModel { PowerViewModel(get(), get(), get()) }
    viewModel { CoachPracticeViewModel(get(), get(), get()) }
    viewModel { VideoResultViewModel(get(), get(), get()) }
    viewModel { ChallengeViewModel(get(), get(), get()) }
    viewModel { ChallengeDetailViewModel(get(), get(), get()) }
    viewModel { ChallengeMoreViewModel(get(), get(), get()) }
    viewModel { ChatRoomViewModel(get(), get(), get()) }
    viewModel { ChatMessageViewModel(get(), get(), get()) }
    viewModel { RankingViewModel(get(), get(), get()) }
    viewModel { CoachGroupViewModel(get(), get(), get()) }
    viewModel { DiscipleListViewModel(get(), get(), get()) }
    viewModel { DiscipleWaitingViewModel(get(), get(), get()) }
    viewModel { CoachGroupDetailMemberViewModel(get(), get(), get()) }
    viewModel { CoachRegisterViewModel(get(), get(), get()) }
    viewModel { EditVideoViewModel(get(), get(), get()) }
    viewModel { VideoRecordViewModel(get(), get(), get()) }
    viewModel { NewsDetailViewModel(get(), get(), get()) }
    viewModel { NotificationViewModel(get(), get(), get()) }
    viewModel { NotificationDetailViewModel(get(), get(), get()) }
    viewModel { PracticeFolderViewModel(get(), get(), get()) }
    viewModel { CoachDraftViewModel(get(), get(), get()) }
    viewModel { CoachGroupDetailMessageViewModel(get(), get(), get()) }
    viewModel { CoachAssignExerciseViewModel(get(), get(), get()) }
    viewModel { CoachGroupDetailLessonViewModel(get(), get(), get()) }
    viewModel { AssignedExerciseViewModel(get(), get(), get()) }
    viewModel { CoachSaveDraftViewModel(get(), get(), get()) }
    viewModel { AddMoreVideoViewModel(get(), get(), get()) }
    viewModel { CoachCreateCourseViewModel(get(), get(), get()) }
    viewModel { CoachSessionChooseFolderViewModel(get(), get(), get()) }
    viewModel { CoachSessionViewModel(get(), get(), get()) }
    viewModel { CoachSessionSavedListViewModel(get(), get(), get()) }
    viewModel { CoachSessionListOldViewModel(get(), get(), get()) }
}