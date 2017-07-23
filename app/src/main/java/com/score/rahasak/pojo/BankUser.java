package com.score.rahasak.pojo;

import android.os.Parcel;

import com.score.senzc.pojos.User;

/**
 * Created by Lakmal on 7/15/17.
 */

//public class BankUser extends SecretUser {
//
//    private String fullName;
//
//    public BankUser(String id, String username, String fullName) {
//        super(id, username);
//        this.fullName = fullName;
//    }
//
//    public BankUser(Parcel in) {
//        super(in);
//        this.fullName = in.readString();
//    }
//
//    public String getFullName() {
//        return fullName;
//    }
//
////    /**
////     * Define the kind of object that you gonna parcel,
////     * You can use hashCode() here
////     */
////    @Override
////    public int describeContents() {
////        return 0;
////    }
////
////    /**
////     * Actual object serialization happens here, Write object content
////     * to parcel one by one, reading should be done according to this write order
////     *
////     * @param dest  parcel
////     * @param flags Additional flags about how the object should be written
////     */
////    @Override
////    public void writeToParcel(Parcel dest, int flags) {
////        super.writeToParcel(dest, flags);
////        dest.writeString(fullName);
////    }
////
////    /**
////     * This field is needed for Android to be able to
////     * create new objects, individually or as arrays
////     * <p>
////     * If you don’t do that, Android framework will through exception
////     * Parcelable protocol requires a Parcelable.Creator object called CREATOR
////     */
////    public static final Creator<BankUser> CREATOR = new Creator<BankUser>() {
////        public BankUser createFromParcel(Parcel in) {
////            return new BankUser(in);
////        }
////
////        public BankUser[] newArray(int size) {
////            return new BankUser[size];
////        }
////    };
////
////    public String getFullName() {
////        return fullName;
////    }
////
////    public void setFullName(String fullName) {
////        this.fullName = fullName;
////    }
//}
