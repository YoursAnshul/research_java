package com.proep.api.controllers;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proep.api.models.business.AdminOptionVariable;
import com.proep.api.models.business.ConfigurationVariable;
import com.proep.api.models.business.FormFieldVariable;
import com.proep.api.models.business.FormFiledIdWrapper;
import com.proep.api.models.business.FrontEndConfiguration;
import com.proep.api.models.business.GeneralResponse;
import com.proep.api.models.business.ProjectIdWrapper;
import com.proep.api.models.business.SessionUserEmail;
import com.proep.api.models.business.SuccessResponse;
import com.proep.api.models.dataaccess.AdminOption;
import com.proep.api.models.dataaccess.BlockOutDate;
import com.proep.api.models.dataaccess.DropDownValue;
import com.proep.api.models.dataaccess.FormField;
import com.proep.api.models.dataaccess.repos.AdminOptionRepository;
import com.proep.api.models.dataaccess.repos.BlockOutDateRepository;
import com.proep.api.models.dataaccess.repos.CommunicationsHubRepository;
import com.proep.api.models.dataaccess.repos.DropDownValueRepository;
import com.proep.api.models.dataaccess.repos.FormFieldRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@Scope("session")
@RequestMapping("/api/configuration")
public class ConfigurationController {

	@Autowired
	private AdminOptionRepository adminOptionRepository;
	@Autowired
	private DropDownValueRepository dropDownValueRepository;
	@Autowired
	private FormFieldRepository formFieldRepository;
	@Autowired
	private BlockOutDateRepository blockOutDateRepository;
	@Autowired
	private CommunicationsHubRepository communicationsHubRepository;

	//front-end configuration
	@Value("${spring.profiles.active:}")
	private String activeProfile;
	@Value("${spring.profiles.active}")
	private String Environment;
	@Value("${pro.api.version}")
	private String version;
	@Value("${pro.api.AssertionUrl}")
	private String AssertionUrl;
	@Value("${pro.api.AppBaseUrl}")
	private String Realm;
	@Value("${pro.api.AppBaseUrl}")
	private String AppBaseUrl;
	@Value("${pro.api.DataAPIUrl}")
	private String DataAPIUrl;

	@Autowired
	private SessionUserEmail UserEmail;

	@ModelAttribute("UserEmail")
	public SessionUserEmail getUserEmail() {
		return UserEmail;
	}

	@GetMapping
	public GeneralResponse index() {
		GeneralResponse response = new GeneralResponse();
		try {
			// get configuration variables
			List<AdminOptionVariable> configurationVariables = getAdminOptionVariables("ALL");
			// get form fields
			List<FormFieldVariable> formFields = getFormFields(true, "ALL");

			formFields = formFields.stream()
					.sorted((ff1, ff2) -> Integer.compare(ff1.getFormField().getFormOrder(),
							ff2.getFormField().getFormOrder()))
					.collect(Collectors.toList());

			response.Status = "Success";
			response.Message = "Successfully retrieved configuration variables";
			response.Subject = new ConfigurationVariable(configurationVariables, formFields);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/frontEndConfiguration")
	public GeneralResponse getFrontEndConfiguration()
	{
		if (Realm != null) {
			if (Realm.contains("/")) {
				if (Realm.substring(Realm.length() - 1, Realm.length()).equals("/")) {
					Realm = Realm.substring(0, Realm.length() - 1);
				}
			}
		}

		GeneralResponse response = new GeneralResponse();
		response.Status = "Success";
		response.Message = "Successfully retrieved front-end configuration";
		response.Subject = new FrontEndConfiguration(activeProfile, version, AssertionUrl, Realm, AppBaseUrl, DataAPIUrl);

		return response;
	}

	@GetMapping("/adminOptions/{fieldName}")
	public GeneralResponse getAdminOptions(@PathVariable String fieldName) {
		GeneralResponse response = new GeneralResponse();
		try {
			// Get admin options based on fieldName
			AdminOptionVariable configurationVariable = getAdminOptionVariables(fieldName).stream().findFirst()
					.orElse(null);

			response.Status = "Success";
			response.Message = "Successfully retrieved admin options";
			response.Subject = configurationVariable;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@PutMapping("/adminOptionVariables")
	public GeneralResponse saveAdminOptionVariables(@RequestBody List<AdminOptionVariable> adminOptionVariables) {
		GeneralResponse response = new GeneralResponse();
		try {
			List<String> errors = new ArrayList<String>();
			int successCount = 0;
			for (AdminOptionVariable adminOptionVariable : adminOptionVariables) {
				try {
					AdminOption adminOptions = adminOptionRepository
							.findByAdminOptionsId(adminOptionVariable.getAdminOptionsId());
					adminOptions.setOptionValue(adminOptionVariable.getOptionValue());
					adminOptionRepository.save(adminOptions);
					successCount++;
				} catch (Exception ex) {
					errors.add(String.format("Configuration variable ID '%s': %s",
							adminOptionVariable.getAdminOptionsId(), ex.getMessage()));
				}
			}
			if (errors.size() > 0) {
				if (successCount < 1)
					throw new Exception("No configuration variables saved to database");
			}

			response.Status = "Success";
			response.Message = "Successfully saved configuration variables";
			response.Subject = new SuccessResponse(successCount, errors);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PutMapping("/formFieldVariables")
	public GeneralResponse saveFormFields(@RequestBody List<FormFieldVariable> formFields) {
		GeneralResponse response = new GeneralResponse();
		try {
			List<String> errors = new ArrayList<String>();
			int successCount = 0;
			for (FormFieldVariable formField : formFields) {
				try {
					FormField savedFormField = formFieldRepository.save(formField.getFormField());
					List<DropDownValue> previousDvs = dropDownValueRepository
							.findByFormFieldId(savedFormField.getFormFieldId());
					dropDownValueRepository.deleteAll(previousDvs);
					List<DropDownValue> currentDvs = new ArrayList<DropDownValue>();
					for (DropDownValue dv : formField.getDropDownValues()) {
						dv.setDropDownValueId(0);
						dv.setFormFieldId(savedFormField.getFormFieldId());
						currentDvs.add(dv);
					}
					dropDownValueRepository.saveAll(currentDvs);
					successCount++;
				} catch (Exception ex) {
					errors.add(String.format("Form field '%s': %s", formField.getFormField().getFieldLabel(),
							ex.getMessage()));
				}
			}
			if (errors.size() > 0) {
				if (successCount < 1)
					throw new Exception("No form fields saved to database");
			}

			response.Status = "Success";
			response.Message = "Successfully saved admin options";
			response.Subject = new SuccessResponse(successCount, errors);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@PutMapping("/dropDownLists")
	public GeneralResponse saveCodeLists(@RequestBody List<FormFieldVariable> formFields) {
		GeneralResponse response = new GeneralResponse();
		try {
			List<String> errors = new ArrayList<String>();
			int successCount = 0;
			for (FormFieldVariable formField : formFields) {
				for (DropDownValue dropDownValue : formField.getDropDownValues()) {
					try {
						dropDownValueRepository.save(dropDownValue);
						successCount++;
					} catch (Exception ex) {
						errors.add(String.format("Dropdown value '%s' for form field '%s': %s",
								dropDownValue.getDropDownItem(), formField.getFormField().getFieldLabel(),
								ex.getMessage()));
					}
				}
			}
			if (errors.size() > 0) {
				if (successCount < 1)
					throw new Exception("No dropdown value saved to database");
			}

			response.Status = "Success";
			response.Message = "Successfully saved dropdown values";
			response.Subject = new SuccessResponse(successCount, errors);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

	@GetMapping("/languages")
	public GeneralResponse getLanguages() {
		GeneralResponse response = new GeneralResponse();
		try {
			FormFieldVariable formField = getFormFields(false, "Language").stream().findFirst().orElse(null);

			response.Status = "Success";
			response.Message = "Successfully retrieved languages";
			response.Subject = formField;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@GetMapping("/formField/{fieldName}")
	public GeneralResponse getFormField(@PathVariable String fieldName) {
		GeneralResponse response = new GeneralResponse();
		try {
			// get form fields
			FormFieldVariable formField = getFormFields(false, fieldName).stream().findFirst().orElse(null);

			response.Status = "Success";
			response.Message = "Successfully retrieved form field for " + fieldName;
			response.Subject = formField;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@GetMapping("/formFieldsByTable/{tableName}")
	public GeneralResponse formFieldsByTable(@PathVariable String tableName) {
		GeneralResponse response = new GeneralResponse();
		try {
			response.Status = "Success";
			response.Message = "Successfully retrieved form fields for " + tableName;
			response.Subject = getFormFieldsByTable(tableName);
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@GetMapping("/userFields")
	public GeneralResponse getUserFields() {
		GeneralResponse response = new GeneralResponse();
		try {
			// get form fields
			List<FormFieldVariable> formFields = getFormFields(false, "ALL");
			formFields = formFields.stream()
					.filter(ff -> ff.getFormField().getTableName().toUpperCase().equals("USERS"))
					.sorted((ff1, ff2) -> Integer.compare(ff1.getFormField().getFormOrder(),
							ff2.getFormField().getFormOrder()))
					.collect(Collectors.toList());
			response.Status = "Success";
			response.Message = "Successfully retrieved user fields";
			response.Subject = formFields;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@GetMapping("/projectFields")
	public GeneralResponse getProjectFields() {
		GeneralResponse response = new GeneralResponse();
		try {
			// get form fields
			List<FormFieldVariable> formFields = getFormFields(false, "ALL");
			formFields = formFields.stream()
					.filter(ff -> ff.getFormField().getTableName().toUpperCase().equals("PROJECTS"))
					.collect(Collectors.toList());
			response.Status = "Success";
			response.Message = "Successfully retrieved project fields";
			response.Subject = formFields;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@GetMapping("/blockOutDates")
	public GeneralResponse getBlockoutDate() {
		GeneralResponse response = new GeneralResponse();
		try {
			LocalDate currentDate = LocalDate.now();
			OffsetDateTime odt = currentDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
			// get form fields
			List<BlockOutDate> blockOutDates = blockOutDateRepository.getBlockOutDatesAfterCurrentDate(odt);
			response.Status = "Success";
			response.Message = "Successfully retrieved project fields";
			response.Subject = blockOutDates;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@PostMapping("/blockOutDates")
	public GeneralResponse saveBlockoutDate(@RequestBody BlockOutDate blockOutDate) {
		GeneralResponse response = new GeneralResponse();
		try {
			BlockOutDate savedBlockOutDate = blockOutDateRepository.save(blockOutDate);
			response.Status = "Success";
			response.Message = "Successfully saved block-out date";
			response.Subject = savedBlockOutDate;
		} catch (Exception ex) {
			response.Status = "Error saving block-out date";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@DeleteMapping("/blockOutDates")
	public GeneralResponse deleteBlockoutDate(@RequestBody BlockOutDate blockOutDate) {
		GeneralResponse response = new GeneralResponse();
		try {
			blockOutDateRepository.delete(blockOutDate);
			response.Status = "Success";
			response.Message = "Successfully deleted block-out date";
		} catch (Exception ex) {
			response.Status = "Error deleting block-out date";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@PostMapping("/commHubConfig")
	public GeneralResponse addDefaultCommHubConfig(HttpServletRequest request,
			@RequestBody ProjectIdWrapper projectIdWrapper) {
		GeneralResponse response = new GeneralResponse();
		try {
			String netId = "Unknown";

			// Retrieve NetId from session if available
			Object netIdObj = request.getSession().getAttribute("NetId");
			if (netIdObj != null) {
				netId = (String) netIdObj;
			}
			int projectId = projectIdWrapper.getProjectId();
			List<FormField> existingProjectFields = formFieldRepository
					.findByTableNameIgnoreCaseAndProjectId("COMMUNICATIONS HUB", projectId);
			if (existingProjectFields.size() < 1) {
				List<FormField> defaultFields = formFieldRepository
						.findDefaultFieldsByProjectTypeAndProjectDisplayIdAndTableName(4, "4", "COMMUNICATIONS HUB");
				for (FormField field : defaultFields) {

					FormField newFormField = (FormField) field.clone();
					newFormField.setFormFieldId(0); // Set the FormFieldId to 0
					newFormField.setProjectId(projectId); // Set the projectId
					FormField savedFormField = formFieldRepository.save(newFormField);
					List<DropDownValue> dropDownValues = dropDownValueRepository
							.findByFormFieldId(field.getFormFieldId());
					List<DropDownValue> newDropDownValues = dropDownValues.stream().map(dropDownValue -> {
						DropDownValue newDropdownValue = null;
						try {
							newDropdownValue = (DropDownValue) dropDownValue.clone();
							newDropdownValue.setDropDownValueId(0);
							newDropdownValue.setFormFieldId(savedFormField.getFormFieldId());

						} catch (CloneNotSupportedException e) {
						}
						return newDropdownValue;
					}).collect(Collectors.toList());
					dropDownValueRepository.saveAll(newDropDownValues);
					response.Status = "Success";
					response.Message = "Successfully added new project using master configuration";
					response.Subject = defaultFields;
				}
			} else {
				response.Status = "Success";
				response.Message = "Project already contains configuration fields";
			}
		} catch (Exception ex) {
			response.Status = "Error adding new project using master configuration";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@DeleteMapping("/commHubConfig")
	public GeneralResponse deleteCommHubConfig(HttpServletRequest request,
			@RequestBody ProjectIdWrapper projectIdWrapper) {
		GeneralResponse response = new GeneralResponse();
		try {
			int projectId = projectIdWrapper.getProjectId();
			// only delete a project's configuration if there are no values recorded
			int usedCount = communicationsHubRepository.countByProjectIdAndTableName(projectId, "COMMUNICATIONS HUB");
			if (usedCount < 1) {
				List<FormField> projectFields = formFieldRepository
						.findByTableNameIgnoreCaseAndProjectId("COMMUNICATIONS HUB", projectId);
				for (FormField field : projectFields) {
					List<DropDownValue> dropDownValues = dropDownValueRepository
							.findByFormFieldId(field.getFormFieldId());
					if (!dropDownValues.isEmpty()) {
						dropDownValueRepository.deleteAll(dropDownValues);
					}
					formFieldRepository.delete(field);
				}
			}
			response.Status = "Success";
			response.Message = "Successfully deleted communications hub configuration";
		} catch (Exception ex) {
			response.Status = "Error deleting communications hub configuration";
			response.Message = ex.getMessage();
		}
		return response;
	}

	@DeleteMapping("/commHubFormField")
	public GeneralResponse deleteCommHubFormField(HttpServletRequest request,
			@RequestBody FormFiledIdWrapper formFiledIdWrapper) {
		GeneralResponse response = new GeneralResponse();
		try {
			int formFieldId = formFiledIdWrapper.getFormFieldId();
			// only delete a project's configuration if there are no values recorded
			int usedCount = communicationsHubRepository.countByFormFieldIdAndTableName(formFieldId,
					"COMMUNICATIONS HUB");
			if (usedCount > 0)
				throw new Exception("Form field cannot be deleted because a value is recorded for this form field");
			FormField formField = formFieldRepository.findByTableNameIgnoreCaseAndFormFieldId("COMMUNICATIONS HUB",
					formFieldId);

			if (formField != null) {
				List<DropDownValue> dropDownValues = dropDownValueRepository
						.findByFormFieldId(formField.getFormFieldId());
				if (!dropDownValues.isEmpty()) {
					dropDownValueRepository.deleteAll(dropDownValues);
				}
				formFieldRepository.delete(formField);
			}
			response.Status = "Success";
			response.Message = "Successfully deleted communications hub form field";
		} catch (Exception ex) {
			response.Status = "Error deleting communications hub form field";
			response.Message = ex.getMessage();
		}
		return response;
	}

	/// <summary>
	/// Returns list of admin option variables with any dropdown values attached
	/// </summary>
	/// <param name="adminOptionName"></param>
	/// <returns></returns>
	private List<AdminOptionVariable> getAdminOptionVariables(String adminOptionName) {
		if (adminOptionName == null || adminOptionName.isEmpty()) {
			adminOptionName = "ALL";
		}
		List<AdminOptionVariable> adminOptionVariables = adminOptionRepository.getAdminOptionVariables(adminOptionName);
		if (!adminOptionVariables.isEmpty()) {
			List<Integer> formFiledIds = adminOptionVariables.stream().map(AdminOptionVariable::getFormFieldId)
					.collect(Collectors.toList());
			if (!formFiledIds.isEmpty()) {
				List<DropDownValue> configurationDropDownValues = dropDownValueRepository
						.getDropDownValuesByFormFieldIds(formFiledIds);
				if (!configurationDropDownValues.isEmpty()) {
					Map<Integer, List<DropDownValue>> groupedAdminOptionVariable = configurationDropDownValues.stream()
							.collect(Collectors.groupingBy(DropDownValue::getFormFieldId));
					for (AdminOptionVariable configuration : adminOptionVariables) {
						if (groupedAdminOptionVariable.containsKey(configuration.getFormFieldId())) {
							configuration
									.setDropDownValues(groupedAdminOptionVariable.get(configuration.getFormFieldId()));
						}
					}
				}
			}
		}
		return adminOptionVariables;
	}

	/// <summary>
	/// Returns list of form fields with any dropdown values attached
	/// </summary>
	/// <param name="configurableOnly"></param>
	/// <param name="fieldName"></param>
	/// <returns></returns>
	private List<FormFieldVariable> getFormFields(boolean configurableOnly, String fieldName) {
		if (fieldName == null || fieldName.isEmpty()) {
			fieldName = "ALL";
		}
		// get form fields
		List<String> configurableOptions = configurableOnly ? Arrays.asList("TRUE") : Arrays.asList("TRUE", "FALSE");
		List<FormFieldVariable> formFields = formFieldRepository
				.getFormFieldVariableByFiledNameAndConfigurableOptions(fieldName, configurableOptions);
		if (!formFields.isEmpty()) {
			List<Integer> formFiledIds = formFields.stream().map(ff -> ff.getFormField().getFormFieldId())
					.collect(Collectors.toList());
			if (!formFiledIds.isEmpty()) {
				List<DropDownValue> configurationDropDownValues = dropDownValueRepository
						.getDropDownValuesByFormFieldIds(formFiledIds);
				if (!configurationDropDownValues.isEmpty()) {
					Map<Integer, List<DropDownValue>> groupedAdminOptionVariable = configurationDropDownValues.stream()
							.collect(Collectors.groupingBy(DropDownValue::getFormFieldId));
					for (FormFieldVariable formField : formFields) {
						if (formField.getFormField() != null
								&& groupedAdminOptionVariable.containsKey(formField.getFormField().getFormFieldId())) {
							formField.setDropDownValues(
									groupedAdminOptionVariable.get(formField.getFormField().getFormFieldId()));
						}
					}
				}
			}
		}
		return formFields;
	}

	/// <summary>
	/// Returns list of form fields, by table, with any dropdown values attached
	/// </summary>
	/// <param name="tableName"></param>
	/// <returns></returns>
	private List<FormFieldVariable> getFormFieldsByTable(String tableName) {
		// get form fields
		List<FormFieldVariable> formFields = formFieldRepository.getFormFieldVariableByTableName(tableName);
		if (!formFields.isEmpty()) {
			List<Integer> formFiledIds = formFields.stream().map(ff -> ff.getFormField().getFormFieldId())
					.collect(Collectors.toList());
			if (!formFiledIds.isEmpty()) {
				List<DropDownValue> configurationDropDownValues = dropDownValueRepository
						.getDropDownValuesByFormFieldIds(formFiledIds);
				if (!configurationDropDownValues.isEmpty()) {
					Map<Integer, List<DropDownValue>> groupedAdminOptionVariable = configurationDropDownValues.stream()
							.collect(Collectors.groupingBy(DropDownValue::getFormFieldId));
					for (FormFieldVariable formField : formFields) {
						if (formField.getFormField() != null
								&& groupedAdminOptionVariable.containsKey(formField.getFormField().getFormFieldId())) {
							formField.setDropDownValues(
									groupedAdminOptionVariable.get(formField.getFormField().getFormFieldId()));
						}
					}
				}
			}
		}
		// sort by form field
		Collections.sort(formFields, Comparator.comparingInt(ff -> ff.getFormField().getFormOrder()));

		return formFields;
	}
}
