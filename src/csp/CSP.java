package csp;

import java.time.LocalDate;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.*;


/**
 * CSP: Calendar Satisfaction Problem Solver
 * Provides a solution for scheduling some n meetings in a given
 * period of time and according to some set of unary and binary 
 * constraints on the dates of each meeting.
 * @author parkerbath
 */
public class CSP {

    /**
     * Private inner class for dateVariable that contains a list of LocalDates as its domain and the current
     * date that the variable is set to
     */
    private static class dateVariable {
        List<LocalDate> domainofDates = new ArrayList<LocalDate>();
        LocalDate currDate;
        
        /**
         * Constructor:
         * Adds all the days in the given range to the domain
         * LocalDate rangeStart is the first potential day in the domain
         * LocalDate rangeEnd is the last potential day in the domain
         */
        dateVariable(LocalDate rangeStart, LocalDate rangeEnd) {
            while (rangeStart.isBefore(rangeEnd) || rangeStart.isEqual(rangeEnd)) {
                domainofDates.add(rangeStart);
                rangeStart = rangeStart.plusDays(1);
            }
        }
    }
	
	
    /**
     * Public interface for the CSP solver in which the number of meetings,
     * range of allowable dates for each meeting, and constraints on meeting
     * times are specified.
     * @param nMeetings The number of meetings that must be scheduled, indexed from 0 to n-1
     * @param rangeStart The start date (inclusive) of the domains of each of the n meeting-variables
     * @param rangeEnd The end date (inclusive) of the domains of each of the n meeting-variables
     * @param constraints Date constraints on the meeting times (unary and binary for this assignment)
     * @return A list of dates that satisfies each of the constraints for each of the n meetings,
     *         indexed by the variable they satisfy, or null if no solution exists.
     */
    public static List<LocalDate> solve (int nMeetings, LocalDate rangeStart, LocalDate rangeEnd, Set<DateConstraint> constraints) {
        List<dateVariable> variables = new ArrayList<dateVariable>();
        int i = 0;
        while(i < nMeetings) {
        	dateVariable meetings = new dateVariable(rangeStart, rangeEnd);
            variables.add(meetings);
            i++;
        }
        variables = prune(variables, constraints); 
        ArrayList<LocalDate> solution = new ArrayList<LocalDate>();
        return backtracking(variables, constraints, solution);
    }
    
    /**
     * prunes incorrect values from the domains of variables and tests node consistency and constraints
     * constraints is the set of constraints that the solution has to satisfy
     * variables = list of all of the variables and their domains
     * @return only the variables with correct domains
     */
    private static List<dateVariable> prune (List<dateVariable> variables, Set<DateConstraint> constraints) {
        for (dateVariable meetings : variables) {
            int i = 0;
            while(i < meetings.domainofDates.size()) {
                LocalDate rightDate;
                LocalDate date = meetings.domainofDates.get(i);
                for (DateConstraint constraint : constraints) {
                    if (constraint.arity() == 1) {
                        rightDate = ((UnaryDateConstraint) constraint).R_VAL;
                        if (!constraintChecker(date, rightDate, constraint)) {
                            meetings.domainofDates.remove(date);
                            i--;
                            break;
                        }
                    }
                } 
                i++;
            }
        }
        return variables;
    }
    
    /**
     * Method tests if two LocalDates given pass the given constraint
     * "rightDate" is the Date on right side of the constraint
     * "leftDate" is the Date on left side of the constraint
     * "constraint" is the DateConstraint used for testing
     */
    private static boolean constraintChecker(LocalDate leftDate, LocalDate rightDate, DateConstraint constraint) {
        switch (constraint.OP) {
        case "==": 
        	if (leftDate.isEqual(rightDate))  return true; 
        	break;
        case "<":  
        	if (leftDate.isBefore(rightDate)) return true; 
        	break;
        case ">":  
        	if (leftDate.isAfter(rightDate))  return true; 
        	break;
        case "!=": 
        	if (!leftDate.isEqual(rightDate)) return true; 
        	break;
        case "<=": 
        	if (leftDate.isBefore(rightDate) || leftDate.isEqual(rightDate)) return true; 
        	break;
        case ">=": 
        	if (leftDate.isAfter(rightDate) || leftDate.isEqual(rightDate))  return true; 
        	break;
        }
        return false;
    }
    
    /**
     * Uses a backtracking recursive tree to try and find a solution
     * variables is a list of all of the variables and their domains
     * constraints is the set of constraints that the solution has to satisfy
     */
    private static List<LocalDate> backtracking (List<dateVariable> variables, Set<DateConstraint> constraints, ArrayList<LocalDate> solution) {
        if (variables.get(variables.size() - 1).currDate != null) {
            for (dateVariable meetings : variables) {
                solution.add(meetings.currDate);
            }
            return solution;
        }
        dateVariable currMeeting = null;
        for (dateVariable meetings : variables) {
            if (meetings.currDate == null) {
                currMeeting = meetings;
                break;
            }
        }
        for (LocalDate date : currMeeting.domainofDates) {
            currMeeting.currDate = date;
            if (checkDateConsistency(variables, constraints)) {
                List<LocalDate> result = backtracking(variables, constraints, solution);
                if (result != null) {
                    return result;
                }
            }
            currMeeting.currDate = null;
        }
        return null;
    }
    
    /**
     * This method is testing if all of the constraints are satisfied from a given solution
     * the parameter constraints is the set of constraints the solution must satisfy
     * @return boolean "satisfied"
     */
    public static boolean checkDateConsistency (List<dateVariable> variables, Set<DateConstraint> constraints) {
        for (DateConstraint p : constraints) {
            LocalDate leftDate = variables.get(p.L_VAL).currDate,
                      rightDate = (p.arity() == 1) ? ((UnaryDateConstraint) p).R_VAL : variables.get(((BinaryDateConstraint) p).R_VAL).currDate;
            if (leftDate == null || rightDate == null) {
                continue;
            }
            if (!(constraintChecker(leftDate, rightDate, p))) {
                return false;
            }
        }
        return true;
    }
    
}





