package org.flowable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

public class HolidRequest {
	public static void main(String[] args) {
		//1.1��Ϊ���������ṩһ�������ļ�
		ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
			.setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
			.setJdbcUsername("sa")
			.setJdbcPassword("")
			.setJdbcDriver("org.h2.Driver")
			.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		//1.2�����������ļ���������������
		ProcessEngine processEngine = cfg.buildProcessEngine();
		//1.33����������ģ��
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment()
			.addClasspathResource("holiday-request.bpmn20.xml")
			.deploy();
		//1.4����ѯ����ģ���������еĶ�����֤һ���Ƿ���أ��Լ����ڲ��ı�ʾ��ʽ
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId())
				.singleResult();
				System.out.println("Found process definition : " + processDefinition.getName());
				
		//2.1����ȡ����ʵ������---��������ʵ��
				
		Scanner scanner= new Scanner(System.in);
		System.out.println("Who are you?");
		String employee = scanner.nextLine();
		System.out.println("How many holidays do you want to request?");
		Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());
		System.out.println("Why do you need them?");
		String description = scanner.nextLine();
//		String employee = "John";
//		Integer nrOfHolidays = 10;
//		String description = "test instance";
		
		//2.2����������ʵ��
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("employee", employee);
		variables.put("nrOfHolidays", nrOfHolidays);
		variables.put("description", description);
		ProcessInstance processInstance =
		runtimeService.startProcessInstanceByKey("holidayRequest", variables);
		
		//2.3����ȡ����
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
		System.out.println("You have " + tasks.size() + " tasks:");
		for (int i=0; i<tasks.size(); i++) {
			System.out.println((i+1) + ") " + tasks.get(i).getName());
		}
		
		//2.4��ִ������
		System.out.println("Which task would you like to complete?");
		int taskIndex = Integer.valueOf(scanner.nextLine());
		System.out.println("The " + taskIndex + "th task would be executed");
//		System.out.println("Which desc would you provied?");
//		String desc = scanner.nextLine();
//		System.out.println("Which desc  you provied is " + desc);
		
		Task task = tasks.get(taskIndex - 1);
		Map<String, Object> processVariables = taskService.getVariables(task.getId());
		System.out.println(processVariables.get("employee") + " wants " +
		processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");
		
		boolean approved = scanner.nextLine().toLowerCase().equals("y");
		variables = new HashMap<String, Object>();
		variables.put("approved", approved);
		taskService.complete(task.getId(), variables);
		
		//2.5����ʾ���̻��ʱ����
		HistoryService historyService = processEngine.getHistoryService();
		List<HistoricActivityInstance> activities =
		historyService.createHistoricActivityInstanceQuery()
			.processInstanceId(processInstance.getId())
			.finished()
			.orderByHistoricActivityInstanceEndTime().asc()
			.list();
		for (HistoricActivityInstance activity : activities) {
			System.out.println(activity.getActivityId() + " took "
			+ activity.getDurationInMillis() + " milliseconds");
		}
	}
}
