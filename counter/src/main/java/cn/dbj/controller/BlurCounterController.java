package cn.dbj.controller;

import cn.dbj.service.BlurCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


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
    }


    @PostMapping("/gets")
    public Result<List<Counter>> getCounters(@RequestBody Counter counter) {
        return Results.success(blurCounterService.getCounters(counter.getObjId(), counter.getObjType(), counter.getKeys()));
    }*/

    /*@PostMapping("/sets")
    public Result<Counter> setCounters(CountDo counter) {
        return Results.success(blurCounterService.setCounters(counter.getObjId(), counter.getObjType(), counter.getKv()));
    }*/
}
