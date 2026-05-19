package com.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.Result;
import com.mall.dto.SaveAssistantTemplateRequestDTO;
import com.mall.dto.UpdateAssistantSettingsRequestDTO;
import com.mall.entity.AssistantSettings;
import com.mall.entity.AssistantTemplate;
import com.mall.mapper.AssistantSettingsMapper;
import com.mall.mapper.AssistantTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端-智能助手管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/assistant")
public class AdminAssistantController {

    @Autowired
    private AssistantSettingsMapper assistantSettingsMapper;

    @Autowired
    private AssistantTemplateMapper assistantTemplateMapper;

    @GetMapping("/settings")
    public Result<AssistantSettings> getSettings() {
        AssistantSettings settings = assistantSettingsMapper.selectOne(
                new LambdaQueryWrapper<AssistantSettings>()
                        .orderByDesc(AssistantSettings::getId)
                        .last("LIMIT 1")
        );
        if (settings == null) {
            settings = createDefaultSettings();
        }
        return Result.success(settings);
    }

    @PutMapping("/settings")
    public Result<AssistantSettings> updateSettings(@Valid @RequestBody UpdateAssistantSettingsRequestDTO request) {
        AssistantSettings settings = assistantSettingsMapper.selectOne(
                new LambdaQueryWrapper<AssistantSettings>()
                        .orderByDesc(AssistantSettings::getId)
                        .last("LIMIT 1")
        );
        if (settings == null) {
            settings = new AssistantSettings();
            settings.setCreateTime(LocalDateTime.now());
        }
        settings.setName(request.getName().trim());
        settings.setEnabled(request.getEnabled());
        settings.setResponseMode(request.getResponseMode().trim());
        settings.setTimeout(request.getTimeout());
        settings.setUpdateTime(LocalDateTime.now());

        if (settings.getId() == null) {
            assistantSettingsMapper.insert(settings);
        } else {
            assistantSettingsMapper.updateById(settings);
        }

        log.info("更新智能助手设置成功: name={}, enabled={}, mode={}, timeout={}",
                settings.getName(), settings.getEnabled(), settings.getResponseMode(), settings.getTimeout());
        return Result.success(settings);
    }

    @GetMapping("/templates")
    public Result<List<AssistantTemplate>> getTemplates() {
        List<AssistantTemplate> templates = assistantTemplateMapper.selectList(
                new LambdaQueryWrapper<AssistantTemplate>()
                        .orderByDesc(AssistantTemplate::getUpdateTime)
                        .orderByDesc(AssistantTemplate::getId)
        );
        return Result.success(templates);
    }

    @PostMapping("/templates")
    public Result<AssistantTemplate> createTemplate(@Valid @RequestBody SaveAssistantTemplateRequestDTO request) {
        AssistantTemplate template = new AssistantTemplate();
        template.setKeyword(request.getKeyword().trim());
        template.setResponse(request.getResponse().trim());
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        assistantTemplateMapper.insert(template);
        return Result.success(template);
    }

    @PutMapping("/templates/{id}")
    public Result<AssistantTemplate> updateTemplate(@PathVariable Long id,
                                                    @Valid @RequestBody SaveAssistantTemplateRequestDTO request) {
        AssistantTemplate template = assistantTemplateMapper.selectById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        template.setKeyword(request.getKeyword().trim());
        template.setResponse(request.getResponse().trim());
        template.setUpdateTime(LocalDateTime.now());
        assistantTemplateMapper.updateById(template);
        return Result.success(template);
    }

    @DeleteMapping("/templates/{id}")
    public Result<?> deleteTemplate(@PathVariable Long id) {
        AssistantTemplate template = assistantTemplateMapper.selectById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        assistantTemplateMapper.deleteById(id);
        return Result.success("删除成功");
    }

    private AssistantSettings createDefaultSettings() {
        AssistantSettings settings = new AssistantSettings();
        settings.setName("商城智能助手");
        settings.setEnabled(1);
        settings.setResponseMode("intelligent");
        settings.setTimeout(30);
        settings.setCreateTime(LocalDateTime.now());
        settings.setUpdateTime(LocalDateTime.now());
        assistantSettingsMapper.insert(settings);
        return settings;
    }
}
