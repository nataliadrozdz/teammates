package teammates.common.datatransfer;

import static teammates.common.Common.EOL;
import teammates.common.Assumption;
import teammates.common.Common;
import teammates.common.FieldValidator;
import static teammates.common.FieldValidator.*;
import teammates.common.exception.InvalidParametersException;
import teammates.storage.entity.Student;

public class StudentAttributes extends EntityAttributes {
	public enum UpdateStatus {
		// @formatter:off
		ERROR(0), NEW(1), MODIFIED(2), UNMODIFIED(3), NOT_IN_ENROLL_LIST(4), UNKNOWN(
				5);
		// @formatter:on

		public final int numericRepresentation;

		private UpdateStatus(int numericRepresentation) {
			this.numericRepresentation = numericRepresentation;
		}

		public static UpdateStatus enumRepresentation(int numericRepresentation) {
			switch (numericRepresentation) {
			case 0:
				return ERROR;
			case 1:
				return NEW;
			case 2:
				return MODIFIED;
			case 3:
				return UNMODIFIED;
			case 4:
				return NOT_IN_ENROLL_LIST;
			case 5:
				return UNKNOWN;
			default:
				return UNKNOWN;
			}
		}
	}

	public String id;
	public String name;
	public String email;
	public String course = null;
	public String comments = null;
	public String team = null;
	public String key = null;

	public UpdateStatus updateStatus = UpdateStatus.UNKNOWN;

		
	public static final String ERROR_ENROLL_LINE_NULL = "Enroll line was null\n";
	public static final String ERROR_ENROLL_LINE_EMPTY = "Enroll line was empty\n";
	public static final String ERROR_ENROLL_LINE_TOOFEWPARTS = "Enroll line had too few parts\n";
	public static final String ERROR_ENROLL_LINE_TOOMANYPARTS = "Enroll line had too many parts\n";
		
	public StudentAttributes(String id, String email, String name, String comments,
			String courseId, String team) {
		this();
		this.id = Common.trimIfNotNull(id);
		this.email = Common.trimIfNotNull(email);
		this.course = Common.trimIfNotNull(courseId);
		this.name = Common.trimIfNotNull(name);
		this.comments = Common.trimIfNotNull(comments);
		this.team = Common.trimIfNotNull(team);
	}

	public StudentAttributes() {
		
	}

	// This is the only entity constructor that throws IPE, because of the way it takes input
	public StudentAttributes(String enrollLine, String courseId)
			throws InvalidParametersException {

		this();
		int TEAM_POS = 0;
		int NAME_POS = 1;
		int EMAIL_POS = 2;
		int COMMENT_POS = 3;

		Assumption.assertNotNull(ERROR_ENROLL_LINE_NULL, enrollLine);
			
		if (enrollLine.equals("")) {
			throw new InvalidParametersException(ERROR_ENROLL_LINE_EMPTY);
		}

		String[] parts = enrollLine.replace("|", "\t").split("\t");

		if (parts.length < 3) {
			throw new InvalidParametersException(ERROR_ENROLL_LINE_TOOFEWPARTS);
		} else if (parts.length > 4) {
			throw new InvalidParametersException(ERROR_ENROLL_LINE_TOOMANYPARTS);
		}

		String paramCourseId = courseId == null ? null : courseId.trim();

		String paramTeam = parts[TEAM_POS].trim();

		String paramName = parts[NAME_POS].trim();

		String paramEmail = parts[EMAIL_POS].trim();

		String paramComment = ((parts.length == 4) ? parts[COMMENT_POS].trim() : "");

		this.team = paramTeam;
		this.name = paramName;
		this.email = paramEmail;
		this.course = paramCourseId;
		this.comments = paramComment;
	}

	public StudentAttributes(Student student) {
		this();
		this.email = student.getEmail();
		this.course = student.getCourseId();
		this.name = student.getName();
		this.comments = ((student.getComments() == null) ? "" : student.getComments());
		this.team = ((student.getTeamName() == null) ? "" : student.getTeamName());
		this.id = ((student.getGoogleId() == null) ? "" : student.getGoogleId());
		Long keyAsLong = student.getRegistrationKey();
		this.key = (keyAsLong == null ? null : Student
				.getStringKeyForLongKey(keyAsLong));

		// TODO: this if for backward compatibility with old system. Old system
		// considers "" as unregistered. It should be changed to consider
		// null as unregistered.
	}

	public boolean isEnrollInfoSameAs(StudentAttributes otherStudent) {
		return (otherStudent != null) && otherStudent.email.equals(this.email)
				&& otherStudent.course.equals(this.course)
				&& otherStudent.name.equals(this.name)
				&& otherStudent.comments.equals(this.comments)
				&& otherStudent.team.equals(this.team);
	}

	public boolean isEnrollmentInfoMatchingTo(StudentAttributes other) {
		return (this.email.equals(other.email))
				&& (this.course.equals(other.course))
				&& (this.name.equals(other.name))
				&& (this.comments.equals(other.comments))
				&& (this.team.equals(other.team))
				&& (this.updateStatus == other.updateStatus);
	}

	public String toString() {
		return toString(0);
	}

	public String toString(int indent) {
		String indentString = Common.getIndent(indent);
		StringBuilder sb = new StringBuilder();
		sb.append(indentString + "Student:" + name + "[" + email + "]" + EOL);
		return sb.toString();
	}

	public static void equalizeIrrelevantData(StudentAttributes expectedStudent,
			StudentAttributes actualStudent) {
		// For these fields, we consider null and "" equivalent.
		if ((expectedStudent.id == null) && (actualStudent.id.equals(""))) {
			actualStudent.id = null;
		}
		if ((expectedStudent.team == null) && (actualStudent.team.equals(""))) {
			actualStudent.team = null;
		}
		if ((expectedStudent.comments == null)
				&& (actualStudent.comments.equals(""))) {
			actualStudent.comments = null;
		}

		// prentend keys match because the key is generated on the server side
		// and cannot be anticipated
		if ((actualStudent.key != null)) {
			expectedStudent.key = actualStudent.key;
		}

	}

	public Student toEntity() {
		return new Student(email, name, id, comments, course, team);
	}

	public String getInvalidStateInfo() {
		//id is allowed to be null when the student is not registered
		Assumption.assertTrue(name!=null);
		Assumption.assertTrue(email!=null);
		Assumption.assertTrue(course!=null);
		Assumption.assertTrue(team!=null);
		Assumption.assertTrue(comments!=null);
		
		FieldValidator validator = new FieldValidator();
		
		String errorMessage = 
				(id==null || id.isEmpty()? "": validator.getValidityInfo(FieldType.GOOGLE_ID, id) + EOL) +
				validator.getValidityInfo(FieldType.COURSE_ID, course) + EOL+
				validator.getValidityInfo(FieldType.EMAIL, email) + EOL +
				//team and comments are allowed to be empty
				(team.isEmpty()? "": validator.getValidityInfo(FieldType.TEAM_NAME, team) + EOL) +
				(comments.isEmpty()? "": validator.getValidityInfo(FieldType.STUDENT_ROLE_COMMENTS, comments) + EOL) +
				validator.getValidityInfo(FieldType.PERSON_NAME, name) + EOL;

		return errorMessage.trim();
	}
}
