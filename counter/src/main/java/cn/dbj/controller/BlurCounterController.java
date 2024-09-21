package cn.dbj.controller;

import cn.dbj.framework.starter.convention.result.Result;
import cn.dbj.framework.starter.web.Results;
import cn.dbj.model.CountDTO;
import cn.dbj.service.BlurCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 模糊计数接口
 */
@RestController
@RequestMapping("/blur/counter")
@RequiredArgsConstructor
public class BlurCounterController {


    private final BlurCounterService blurCounterService;

    /*@GetMapping("/get")
    public Result<Counter> getCounter(Counter counter) {
        return Results.success(blurCounterService.getCounter(counter.getObjId(), counter.getObjType(), counter.getCountKey()));
    }

    @PostMapping("/set")
    public Result<Counter> setCounter(@RequestBody CountDo counter) {
        return Results.success(blurCounterService.setCounter(counter.getObjId(), counter.getObjType(), counter.getCountKey(), counter.getCountValue()));
    }*/


    @PostMapping("/gets")
    public Result<List<CountDTO>> getCounters(@RequestBody CountDTO counter) {
        return Results.success(blurCounterService.getCounters(counter.getUid(), counter.getCountKey(), counter.getKeys()));
    }

    @PostMapping("/set")
    public Result<CountDTO> setCounters(@RequestBody CountDTO counter) {
        return Results.success(blurCounterService.setCounter(counter.getUid(), counter.getObjId(), counter.getCountKey(),counter.getCountValue()));

    }
}
