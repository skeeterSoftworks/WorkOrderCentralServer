package com.skeeterSoftworks.WorkOrderCentral.mapper;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Tool;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MachineTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.ToolTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineMapperService {

    public MachineTO mapToTO(Machine machine) {
        if (machine == null) return null;
        MachineTO to = new MachineTO();
        to.setId(machine.getId());
        to.setMachineName(machine.getMachineName());
        to.setCycleTime(machine.getCycleTime());
        to.setBarLocation(machine.getBarLocation());
        to.setPiecesPerBar(machine.getPiecesPerBar());
        to.setBarsPerSeries(machine.getBarsPerSeries());
        to.setBarsCount(machine.getBarsCount());
        to.setWeightPerBar(machine.getWeightPerBar());
        to.setSumBarWeight(machine.getSumBarWeight());
        to.setSeriesID(machine.getSeriesID());
        if (machine.getTools() != null) {
            to.setTools(machine.getTools().stream().map(this::mapToolToTO).collect(Collectors.toList()));
        } else {
            to.setTools(new ArrayList<>());
        }
        return to;
    }

    public Machine mapToEntity(MachineTO to) {
        if (to == null) return null;
        Machine machine = new Machine();
        if (to.getId() != null) {
            machine.setId(to.getId());
        }
        machine.setMachineName(to.getMachineName());
        machine.setCycleTime(to.getCycleTime());
        machine.setBarLocation(to.getBarLocation());
        machine.setPiecesPerBar(to.getPiecesPerBar());
        machine.setBarsPerSeries(to.getBarsPerSeries());
        machine.setBarsCount(to.getBarsCount());
        machine.setWeightPerBar(to.getWeightPerBar());
        machine.setSumBarWeight(to.getSumBarWeight());
        machine.setSeriesID(to.getSeriesID());
        if (to.getTools() != null) {
            List<Tool> tools = to.getTools().stream()
                    .map(toolTO -> mapToolToEntity(toolTO, machine))
                    .collect(Collectors.toList());
            machine.setTools(tools);
        } else {
            machine.setTools(new ArrayList<>());
        }
        return machine;
    }

    public ToolTO mapToolToTO(Tool tool) {
        if (tool == null) return null;
        ToolTO to = new ToolTO();
        to.setId(tool.getId());
        to.setToolName(tool.getToolName());
        to.setToolDescription(tool.getToolDescription());
        return to;
    }

    public Tool mapToolToEntity(ToolTO to, Machine machine) {
        if (to == null) return null;
        Tool tool = new Tool();
        if (to.getId() != null) {
            tool.setId(to.getId());
        }
        tool.setToolName(to.getToolName());
        tool.setToolDescription(to.getToolDescription());
        tool.setMachine(machine);
        return tool;
    }
}
