import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * This is just a demo for you, please run it on JDK17
 * (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
        br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
            Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]),
                    Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]),
                    Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                    Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]),
                    Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                    Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                this.courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
    /**
    * This method returns a <institution, count> map, where the key is the institution
    * while the value is the
    * total number of participants who have accessed the courses of the institution.
    * The map should be sorted by the alphabetical order of the institution
    * */
        Map<String, Integer> result = new HashMap<>();

        //ParticipantsByInstitution: first group the participants and count their sums
        Map<String, Integer> PtcpCountByInst = this.courses.stream()
            .collect(Collectors.groupingBy(Course -> Course.institution,
                Collectors.summingInt(Course -> Course.participants)));

        //Sort ParticipantsByInstitution by the alphabetical order of the key, and put them into a new map called result
        PtcpCountByInst.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> result.put(x.getKey(),x.getValue()));
        return result;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
    /**
     * This method returns a <institution-course Subject, count> map, where the key is the string
     * concatenating the Institution and the course Subject (without quotation marks) using '-'
     * while the value is the total number of participants in a course Subject of an institution.
     * The map should be sorted by descending order of count (i.e., from most to least participants).
     * If two participants have the same count, then they should be sorted by the alphabetical order of the
     * institution-course Subject
     */
        Map<String, Integer> result = new HashMap<>();

        //PtcpByInstAndSubject: 1)concat using '-' 2)value is # of participants
        Map<String, Integer> PtcpByInstAndSubject = this.courses.stream()
            .collect(Collectors.groupingBy(Course -> Course.institution.concat("-"+Course.subject),
                Collectors.summingInt(Course -> Course.participants)));

        //Sort PtcpByInstAndSubject by descending order of value
        //Same value, sort key by alphabetical order
        result = PtcpByInstAndSubject.entrySet().stream().sorted(((o1, o2) -> {
            int compare = -o1.getValue().compareTo(o2.getValue());
            if (compare == 0) {
                String o1_key = o1.getKey();
                String o2_key = o2.getKey();
                int length1 = o1_key.length();
                int length2 = o2_key.length();
                int min;

                //ensure that if characters within min length have been compared and all are same, the shorter one should be at the front -> 1
                //if length1 and length2 are the same, compare shall not change, and check every single charcters
                if (length1 < length2) {
                    compare = 1;
                    min = length1;
                } else if (length1 > length2) {
                    compare = -1;
                    min = length2;
                } else {
                    compare = 0;
                    min = length1;
                }
                for (int i = 0; i < min; i++) {
                    if (o1_key.charAt(i) < o2_key.charAt(i)) {
                        compare = 1;
                        break;
                    } else if (o1_key.charAt(i) > o2_key.charAt(i)) {
                        compare = -1;
                        break;
                    }
                }
            }
            return compare;
        })).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return result;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        /**
         * An instructor may be responsible for multiple courses, including independently responsible courses and codeveloped courses.
         * This method returns a <Instructor, [[course1, course2,...],[coursek,coursek+1,...]]>
         * map, where the key is the name of the instructor (without quotation marks) while the value is a list
         * containing 2-course lists, where List 0 is the instructor's independently responsible courses, if s/he has no
         * independently responsible courses, this list also needs to be created, but with no elements.
         * List 1 is the instructor's co-developed courses, if there are no co-developed courses, do the same as List 0. Note that
         * the course title (without quotation marks) should be sorted by alphabetical order in the list, and the case of
         * identical names should be treated as the same person.
         */
        Map<String, List<List<String>>> instructorsCourses = new HashMap<>();
        Stream<String> names;
        // group courses by instructor names
        Map<String, List<Course>> coursesByInstructors = this.courses.stream()
            .flatMap(course -> Stream.of(course.instructors.split(", ")))
            .distinct()
            .collect(Collectors.toMap(
                instrcutor -> instrcutor,
                instructor -> this.courses.stream()
                    .filter(course -> Arrays.asList(course.instructors.split(", ")).contains(instructor))
                    //we can divide Duflo's
                    //alphabetical order
                    .sorted(Comparator.comparing(course -> course.title))
                    .collect(Collectors.toList()),
                (list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                }
            ));

        // create the map of instructors and their courses
        coursesByInstructors.forEach((instructor, coursesList) -> {
            List<String> independentlyResponsibleCourses = new ArrayList<>();
            List<String> coDevelopedCourses = new ArrayList<>();
            for (Course course: coursesList) {
                if (course.instructors.split(", ").length == 1) { //String[].length == 1 -> independent
                    if (!independentlyResponsibleCourses.contains(course.title)){
                        independentlyResponsibleCourses.add(course.title);
                    }
                } else {
                    if (!coDevelopedCourses.contains(course.title)) {
                        coDevelopedCourses.add(course.title);
                    }
                }
            }
            List<List<String>> instructor2Courses = new ArrayList<>();
            instructor2Courses.add(independentlyResponsibleCourses);
            instructor2Courses.add(coDevelopedCourses);
            instructorsCourses.put(instructor, instructor2Courses);
        });

        return instructorsCourses;

    }

    //4
    public List<String> getCourses(int topK, String by) {
        /**
         * This method returns the top K courses (parameter topK) by the given criterion (parameter by).
         * Specifically,
         * by="hours": the results should be courses sorted by descending order of Total Course Hours
         * (Thousands) (from the longest course to the shortest course).
         * by="participants": the results should be courses sorted by descending order of the number of
         * the Participants (Course Content Accessed) (from the most to the least).
         * Note that the results should be a list of Course titles. If two courses have the same total Course hours or
         * participants, then they should be sorted by alphabetical order of their titles. The same course title can only
         * occur once in the list.
         */
        // Sort the courses according to the given criterion
        Comparator<Course> comparator;
        List<Course> sortedCourses = new ArrayList<>();
        if (by.equals("hours")) {
            comparator = Comparator.comparingDouble((Course course) -> course.totalHours).reversed()
                .thenComparing(course -> course.title);
        } else if (by.equals("participants")) {
            comparator = Comparator.comparing((Course course) -> course.participants).reversed()
                .thenComparing(course -> course.title);
        }
        else {
            //If not, we cut off the program so there won't be error warning then
            throw new IllegalArgumentException("Invalid criterion: " + by);
        }
        sortedCourses = this.courses.stream()
            .distinct()
            .sorted(comparator)
            .collect(Collectors.toList());

        // Extract the top K course titles
        List<String> topKCourses = new ArrayList<>();
        // One title can only be in the list for once, so we can correct it out effciently
        Set<String> courseTitles = new HashSet<>();
        for (Course course : sortedCourses) {
            if (topKCourses.size() >= topK) {
                break;
            }
            String title = course.title;
            if (!courseTitles.contains(title)) {
                topKCourses.add(title);
                courseTitles.add(title);
            }
        }
        return topKCourses;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        /**
         * This method searches courses based on three criteria:
         * courseSubject: Fuzzy matching is supported and case insensitive. If the inputcourseSubject is
         * "science", all courses whose course subject includes "science" or "Science" or whatever (case
         * insensitive) meet the criteria.
         * percentAudited: the percent of the audited should >= percentAudited
         * totalCourseHours: the Total Course Hours (Thousands) should <= totalCourseHours
         * Note that the results should be a list of course titles that meet the given criteria, and sorted by alphabetical
         * order of the titles. The same course title can only occur once in the list.
         */
        List<String> matchingCourses = new ArrayList<>();
        for (Course course : courses) {
            // check if the course subject matches the input subject using a case-insensitive fuzzy search
            if (course.subject.toLowerCase().contains(courseSubject.toLowerCase())) {
                // check if the percent audited is greater than or equal to the input percent
                double percent = ((double) course.audited/course.participants)*100;
                if (percent >= percentAudited) {
                    // check if the total course hours is less than or equal to the input hours
                    // ensure there will only be one title in the list
                    if (course.totalHours <= totalCourseHours && !matchingCourses.contains(course.title)) {
                        matchingCourses.add(course.title);
                    }
                }
            }
        }
        // sort the matching courses by alphabetical order and return the list
        Collections.sort(matchingCourses);
        return matchingCourses;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        /**
         * This method recommends 10 courses based on the following input parameter:
         * age: age of the user
         * gender: 0-female, 1-male
         * isBachelorOrHigher: 0-Not get bachelor degree, 1- Bachelor degree or higher
         The courses should be sorted by their similarity values. If two courses
         * have the same similarity values, then they should be sorted by alphabetical order of their titles
         */
        // Calculate the average Median Age, average % Male, and average % Bachelor's Degree or Higher for each course
        // Mark a course by its course number(unique)
        // Group course by course number
        Map<String, List<Course>> coursesByNumber = new HashMap<>();
        for (Course course : this.courses) {
            String courseNumber = course.number;
            if (!coursesByNumber.containsKey(courseNumber)) {
                coursesByNumber.put(courseNumber, new ArrayList<>());
            }
            //find key and add
            coursesByNumber.get(courseNumber).add(course);
        }

        // every List of courses will be sorted by reversed order to put the latest at first
        coursesByNumber.values().forEach(
            courses -> courses.sort(
                Comparator.comparing((Course course) -> course.launchDate).reversed()
            ));

        // Calculate similarity value by the formula provided
        Map<String, Double> courseAvgByNumber = new HashMap<>();
        for (Map.Entry<String, List<Course>> entry : coursesByNumber.entrySet()) {
            String courseNumber = entry.getKey();
            List<Course> coursesList = entry.getValue();
            int participants = 0;
            int cnt = coursesList.size();
            double totalMedianAge, totalMale, totalDegree;
            totalMedianAge = 0.0;
            totalDegree = 0.0;
            totalMale = 0.0;
            for (Course course : coursesList) {
//                int courseParticipants = course.participants;
//                participants += courseParticipants;
//                totalMedianAge += (course.medianAge * courseParticipants);
//                totalMale += ((course.percentMale / 100.0) * courseParticipants);
//                totalDegree += ((course.percentDegree / 100.0) * courseParticipants);
                totalMedianAge += course.medianAge;
                totalMale += course.percentMale;
                totalDegree += course.percentDegree;
            }
//            double avgMedianAge = (totalMedianAge / participants);
            // # of male / # of overall participants (units as %)
//            double avgPercentMale = ((totalMale / participants) * 100.0);
//            double avgPercentDegree = ((totalDegree / participants) * 100.0);
            double avgMedianAge = totalMedianAge / cnt;
            double avgPercentMale = totalMale / cnt;
            double avgPercentDegree = totalDegree /cnt;
            CourseAvg c = new CourseAvg(avgMedianAge, avgPercentMale, avgPercentDegree);
            // Calculate similarity for each course by id(not for title)
            // id -> similarity
            double similarityValue =
                Math.pow(age - c.avgMedianAge, 2) +
                    Math.pow(gender * 100.0 - c.avgPercentMale, 2) +
                    Math.pow(isBachelorOrHigher * 100.0 - c.avgPercentDegree, 2);
            courseAvgByNumber.put(courseNumber, similarityValue);
        }

        // Return the top 10 courses with the smallest similarity value -> list<title>
        // Find the latest for each id, and exchange courseID to courseTitle
        Map<String, Double> sortedCourses = new HashMap<>();

        for (Map.Entry<String, List<Course>> entry : coursesByNumber.entrySet()) {
            String courseNumber = entry.getKey();
            Course latestCourse = entry.getValue().get(0);
            String latestTitle = latestCourse.title;
            sortedCourses.put(latestTitle, courseAvgByNumber.get(courseNumber));
        }
        Map<String, Double> result;
        result = sortedCourses.entrySet().stream()
            .sorted((Comparator
                .comparingDouble((ToDoubleFunction<Entry<String, Double>>) Entry::getValue)
                .thenComparing(Entry::getKey)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


        // same similarity -> sort by alphabetical orders of their titles

        List<String> top10Courses = new ArrayList<>();
        for(Map.Entry<String, Double> entry:result.entrySet()) {
//            System.out.println(entry.getKey()+" "+entry.getValue());
            if (top10Courses.size() < 10){
                top10Courses.add(entry.getKey());
            } else {
                break;
            }
        }
        return top10Courses;
    }
}

class CourseAvg{
    double avgMedianAge;
    double avgPercentMale;
    double avgPercentDegree;

    public CourseAvg(double avgMedianAge, double avgPercentMale, double avgPercentDegree) {
        this.avgPercentDegree = avgPercentDegree;
        this.avgMedianAge = avgMedianAge;
        this.avgPercentMale = avgPercentMale;
    }
}
class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1); //already left double quotation marks behind
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}