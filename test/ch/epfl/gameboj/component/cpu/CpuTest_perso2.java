package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTest_perso2 {

    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    @Test
    void ADD_A_N8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 42);
        b.write(2, Opcode.ADC_A_N8.encoding);
        b.write(3, 16);

        cycleCpu(c, 4);

        assertArrayEquals(new int[] { 4, 0, 58, 0b0000_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0xff);
        b.write(2, Opcode.ADD_A_A.encoding);

        cycleCpu(c, 4);

        assertArrayEquals(
                new int[] { 4, 0, 0xFE, 0b0011_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_HLR_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 2);
        b.write(2, Opcode.ADD_A_HLR.encoding);

        cycleCpu(c, 3);

        assertArrayEquals(
                new int[] { 3, 0, 200, 0b0000_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 5);
        b.write(2, Opcode.INC_A.encoding);

        cycleCpu(c, 3);

        assertArrayEquals(new int[] { 3, 0, 6, 0b0000_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_HLR_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.INC_HL.encoding);

        cycleCpu(c, 1);

        assertArrayEquals(new int[] { 1, 0, 0, 0b0000_0000, 0, 0, 0, 0, 0, 1 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_R16SP_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.INC_SP.encoding);
        cycleCpu(c, 1);

        assertArrayEquals(new int[] { 1, 1, 0, 0b0000_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_HL_R16SP_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_BC_N16.encoding);
        b.write(1, 0x2);
        b.write(2, 0x3);
        b.write(3, Opcode.ADD_HL_BC.encoding);
        cycleCpu(c, 4);

        assertArrayEquals(new int[] { 4, 0, 0, 0b0000_0000, 3, 2, 0, 0, 3, 2 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_HLSP_S8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_SP_N.encoding);
        b.write(1, 0b1111_1111);
        b.write(2, Opcode.LD_HL_SP_N8.encoding);
        b.write(3, 0b1111_1111);

        cycleCpu(c, 7);

        assertArrayEquals(new int[] { 4, 0xffff, 0, 0b0011_0000, 0, 0, 0, 0,
                0b1111_1111, 0b1111_1110 }, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0x5);
        b.write(2, Opcode.SUB_A_A.encoding);

        cycleCpu(c, 3);

        assertArrayEquals(new int[] { 3, 0, 0, 0b1100_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_N8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0x5);
        b.write(2, Opcode.SUB_A_N8.encoding);
        b.write(3, 2);

        cycleCpu(c, 4);

        assertArrayEquals(new int[] { 4, 0, 3, 0b0100_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_HLR_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0x5);
        b.write(2, Opcode.SUB_A_HLR.encoding);

        cycleCpu(c, 3);

        assertArrayEquals(
                new int[] { 3, 0, 0b0011_1111, 0b0111_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 5);
        b.write(2, Opcode.DEC_A.encoding);

        cycleCpu(c, 3);

        assertArrayEquals(new int[] { 3, 0, 4, 0b0100_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 5);
        b.write(2, Opcode.CP_A_B.encoding);

        cycleCpu(c, 3);

        assertArrayEquals(new int[] { 3, 0, 5, 0b0100_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R16SP_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_BC_N16.encoding);
        b.write(1, 0x2);
        b.write(2, 0x3);

        b.write(3, Opcode.DEC_BC.encoding);

        cycleCpu(c, 4);

        assertArrayEquals(new int[] { 4, 0, 0, 0b0000_0000, 3, 1, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void BIT_U3_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(100);
        Bus b = connect(c, r);

        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0x6);
        b.write(2, 0xCB);
        b.write(3, Opcode.BIT_0_B.encoding);
        cycleCpu(c, 4);
        assertArrayEquals(new int[] { 4, 0, 6, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(4, 0xCB);
        b.write(5, Opcode.BIT_7_H.encoding);
        cycleCpu(c, 6);
        assertArrayEquals(new int[] { 6, 0, 6, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(6, 0xCB);
        b.write(7, Opcode.BIT_5_C.encoding);
        cycleCpu(c, 8);
        assertArrayEquals(new int[] { 8, 0, 6, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(8, 0xCB);
        b.write(9, Opcode.BIT_4_D.encoding);
        cycleCpu(c, 10);
        assertArrayEquals(new int[] { 10, 0, 6, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(10, 0xCB);
        b.write(11, Opcode.BIT_6_L.encoding);
        cycleCpu(c, 12);
        assertArrayEquals(new int[] { 12, 0, 6, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(12, 0xCB);
        b.write(13, Opcode.BIT_2_E.encoding);
        cycleCpu(c, 14);
        assertArrayEquals(new int[] { 14, 0, 6, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());

        b.write(14, 0xCB);
        b.write(15, Opcode.BIT_1_A.encoding);
        cycleCpu(c, 16);
        assertArrayEquals(new int[] { 16, 0, 6, 0b0010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

    @Test
    void BIT_U3_HLR_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(100);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.LD_D_D.encoding); //0x52 = 0b0101_0010
        
        b.write(1, 0xCB);
        b.write(2, Opcode.BIT_0_HLR.encoding);
        cycleCpu(c, 3);
        assertArrayEquals(new int[] { 3, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(3, 0xCB);
        b.write(4, Opcode.BIT_1_HLR.encoding);
        cycleCpu(c, 6);
        assertArrayEquals(new int[] { 5, 0, 0, 0b0010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(5, 0xCB);
        b.write(6, Opcode.BIT_2_HLR.encoding);
        cycleCpu(c, 9);
        assertArrayEquals(new int[] { 7, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(7, 0xCB);
        b.write(8, Opcode.BIT_3_HLR.encoding);
        cycleCpu(c, 12);
        assertArrayEquals(new int[] { 9, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(9, 0xCB);
        b.write(10, Opcode.BIT_4_HLR.encoding);
        cycleCpu(c, 15);
        assertArrayEquals(new int[] { 11, 0, 0, 0b0010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(11, 0xCB);
        b.write(12, Opcode.BIT_5_HLR.encoding);
        cycleCpu(c, 18);
        assertArrayEquals(new int[] { 13, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(13, 0xCB);
        b.write(14, Opcode.BIT_6_HLR.encoding);
        cycleCpu(c, 21);
        assertArrayEquals(new int[] { 15, 0, 0, 0b0010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
        
        b.write(15, 0xCB);
        b.write(16, Opcode.BIT_7_HLR.encoding);
        cycleCpu(c, 24);
        assertArrayEquals(new int[] { 17, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }
    
    @Test void CPL_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0b0001_1001);
        b.write(2, Opcode.CPL.encoding);
        cycleCpu(c,3);

        assertArrayEquals(new int[] { 3, 0, 0b1110_0110, 0b0110_0000, 0,0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }
    
    @Test void ROTCA_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0b0100_0000);
        b.write(2, Opcode.RLCA.encoding);
        cycleCpu(c,3);

        assertArrayEquals(new int[] { 3, 0, 0b1000_0000, 0b0000_0000, 0,0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }
    
    //TODO
    @Test void ROTA_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0b0100_0000);
        b.write(2, Opcode.RLCA.encoding);
        cycleCpu(c,3);

        assertArrayEquals(new int[] { 3, 0, 0b1000_0000, 0b0000_0000, 0,0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }
    
    // kaada etdawer b kaabtin mouch kaaba bark
    @Test void ROTC_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0b0000_0001);
        b.write(2, 0xCB);
        b.write(3, Opcode.RLC_A.encoding);
        cycleCpu(c,4);

        assertArrayEquals(new int[] { 4, 0, 0b0000_0010, 0b0000_0000, 0,0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }
    
    @Test void SWAP_R8_Works() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        
        b.write(0, Opcode.ADD_A_N8.encoding);
        b.write(1, 0b1000_0001);
        b.write(2, 0xCB);
        b.write(3, Opcode.SWAP_A.encoding);
        cycleCpu(c,4);

        assertArrayEquals(new int[] { 4, 0, 0b0001_1000, 0b0000_0000, 0,0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }
}