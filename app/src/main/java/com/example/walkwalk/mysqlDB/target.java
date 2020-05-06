package com.example.walkwalk.mysqlDB;

public class target {
    public static Target myTarget;
    public static class Target{
        public Target(){
        }
        int targetID;
        String ageID;
        String sex;
        int t_stepCount_min;
        int t_stepCount_max;
        float t_walkV_min;
        float t_walkV_max;

        public int getTargetID() {
            return targetID;
        }

        public void setTargetID(int targetID) {
            this.targetID = targetID;
        }

        public String getAgeID() {
            return ageID;
        }

        public void setAgeID(String ageID) {
            this.ageID = ageID;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public int getT_stepCount_min() {
            return t_stepCount_min;
        }

        public void setT_stepCount_min(int t_stepCount_min) {
            this.t_stepCount_min = t_stepCount_min;
        }

        public int getT_stepCount_max() {
            return t_stepCount_max;
        }

        public void setT_stepCount_max(int t_stepCount_max) {
            this.t_stepCount_max = t_stepCount_max;
        }

        public float getT_walkV_min() {
            return t_walkV_min;
        }

        public void setT_walkV_min(float t_walkV_min) {
            this.t_walkV_min = t_walkV_min;
        }

        public float getT_walkV_max() {
            return t_walkV_max;
        }

        public void setT_walkV_max(float t_walkV_max) {
            this.t_walkV_max = t_walkV_max;
        }
    }
}
