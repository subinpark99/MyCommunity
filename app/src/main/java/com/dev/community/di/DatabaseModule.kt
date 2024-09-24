package com.dev.community.di

import android.content.Context
import com.dev.community.util.PreferenceUtil
import com.dev.community.data.repository.CommentRepository
import com.dev.community.data.repository.CommentRepositoryImpl
import com.dev.community.data.repository.PostRepository
import com.dev.community.data.repository.PostRepositoryImpl
import com.dev.community.data.repository.UserRepository
import com.dev.community.data.repository.UserRepositoryImpl
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return Firebase.messaging
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }


    @Provides
    @Singleton
    fun provideUserRepository(userRepositoryImpl: UserRepositoryImpl)
            : UserRepository = userRepositoryImpl

    @Provides
    @Singleton
    fun providePostRepository(postRepositoryImpl: PostRepositoryImpl)
            : PostRepository = postRepositoryImpl

    @Provides
    @Singleton
    fun provideCommentRepository(commentRepositoryImpl: CommentRepositoryImpl)
            : CommentRepository = commentRepositoryImpl


    @Provides
    @Singleton
    fun providePreferenceUtil(context: Context): PreferenceUtil {
        return PreferenceUtil(context)
    }

}